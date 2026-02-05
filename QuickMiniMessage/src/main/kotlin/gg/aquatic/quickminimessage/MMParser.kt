package gg.aquatic.quickminimessage

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.MMTagContext
import gg.aquatic.quickminimessage.tag.resolver.MMDataComponentResolver
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.key.Key
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.util.HSVLike
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.text.iterator

object MMParser {
    @JvmStatic
    fun deserialize(input: String): Component {
        return deserialize(input, MMTagResolver.empty(), MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver): Component {
        return deserialize(input, resolver, MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, dataComponentResolver: MMDataComponentResolver): Component {
        if (input.isEmpty()) {
            return Component.empty()
        }
        if (input.indexOf(TAG_OPEN) < 0 && input.indexOf(ESCAPE) < 0) {
            return Component.text(input)
        }
        return Parser(input, resolver, null, dataComponentResolver).parse()
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, pointered: Pointered?): Component {
        return deserialize(input, resolver, pointered, MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(
        input: String,
        resolver: MMTagResolver,
        pointered: Pointered?,
        dataComponentResolver: MMDataComponentResolver
    ): Component {
        if (input.isEmpty()) {
            return Component.empty()
        }
        if (input.indexOf(TAG_OPEN) < 0 && input.indexOf(ESCAPE) < 0) {
            return Component.text(input)
        }
        return Parser(input, resolver, pointered, dataComponentResolver).parse()
    }

    @JvmStatic
    fun deserialize(input: String, vararg resolvers: MMTagResolver): Component {
        return deserialize(input, MMTagResolver.resolver(*resolvers))
    }

    @JvmStatic
    fun parse(input: String): Component = deserialize(input)

    private class Parser(
        private val input: String,
        private val resolver: MMTagResolver,
        private val pointered: Pointered?,
        private val dataComponentResolver: MMDataComponentResolver
    ) {
        private val frames = ArrayList<Frame>(8)
        private val textBuffer = StringBuilder(64)
        private val context = MMTagContext(resolver, pointered, dataComponentResolver)

        init {
            frames.add(RootFrame())
        }

        fun parse(): Component {
            parseSegment(input)
            flushText()
            while (frames.size > 1) {
                closeTop()
            }
            return frames[0].build()
        }

        private fun parseSegment(source: String) {
            if (source.indexOf(TAG_OPEN) < 0 && source.indexOf(ESCAPE) < 0) {
                textBuffer.append(source)
                return
            }
            var index = 0
            val length = source.length
            while (index < length) {
                val ch = source[index]
                if (ch == ESCAPE) {
                    if (index + 1 < length) {
                        val next = source[index + 1]
                        if (next == TAG_OPEN || next == ESCAPE) {
                            textBuffer.append(next)
                            index += 2
                            continue
                        }
                    }
                    textBuffer.append(ch)
                    index++
                    continue
                }
                if (ch == TAG_OPEN) {
                    val token = readTag(source, index)
                    if (token == null) {
                        textBuffer.append(ch)
                        index++
                        continue
                    }
                    flushText()
                    if (!handleTag(token)) {
                        textBuffer.append(source, token.rawStart, token.rawEnd)
                    }
                    index = token.endIndex
                    continue
                }
                textBuffer.append(ch)
                index++
            }
        }

        private fun flushText() {
            if (textBuffer.isEmpty()) {
                return
            }
            frames.last().children.add(Component.text(textBuffer.toString()))
            textBuffer.setLength(0)
        }

        private fun readTag(source: String, start: Int): TagToken? {
            var index = start + 1
            var inQuote: Char = 0.toChar()
            var escaped = false
            val length = source.length
            while (index < length) {
                val ch = source[index]
                if (inQuote.code != 0) {
                    if (escaped) {
                        escaped = false
                    } else if (ch == ESCAPE) {
                        escaped = true
                    } else if (ch == inQuote) {
                        inQuote = 0.toChar()
                    }
                } else {
                    if (ch == SINGLE_QUOTE || ch == DOUBLE_QUOTE) {
                        inQuote = ch
                    } else if (ch == TAG_CLOSE) {
                        break
                    }
                }
                index++
            }
            if (index >= length || source[index] != TAG_CLOSE) {
                return null
            }
            val contentStart = skipWhitespaceForward(source, start + 1, index)
            val contentEnd = skipWhitespaceBack(source, start + 1, index)
            if (contentStart > contentEnd) {
                return null
            }
            val isClosing = source[contentStart] == '/'
            val isSelfClosing = !isClosing && source[contentEnd] == '/'
            var bodyStart = contentStart + if (isClosing) 1 else 0
            var bodyEnd = contentEnd - if (isSelfClosing) 1 else 0
            bodyStart = skipWhitespaceForward(source, bodyStart, bodyEnd + 1)
            bodyEnd = skipWhitespaceBack(source, bodyStart, bodyEnd + 1)
            if (bodyStart > bodyEnd) {
                return null
            }
            val parts = splitArgs(source, bodyStart, bodyEnd + 1) ?: return null
            val name = asciiLowercase(parts[0])
            val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()
            return TagToken(start, index + 1, name, args, isClosing, isSelfClosing, index + 1)
        }

        private fun splitArgs(source: String, start: Int, endExclusive: Int): List<String>? {
            val result = ArrayList<String>(4)
            val current = StringBuilder()
            var index = start
            var inQuote: Char = 0.toChar()
            while (index < endExclusive) {
                val ch = source[index]
                if (inQuote.code != 0) {
                    if (ch == ESCAPE && index + 1 < endExclusive) {
                        val next = source[index + 1]
                        if (next == inQuote || next == ESCAPE) {
                            current.append(next)
                            index += 2
                            continue
                        }
                    }
                    if (ch == inQuote) {
                        inQuote = 0.toChar()
                        index++
                        continue
                    }
                    current.append(ch)
                    index++
                    continue
                }
                if (ch == ESCAPE && index + 1 < endExclusive) {
                    val next = source[index + 1]
                    if (next == ARG_SEPARATOR || next == ESCAPE) {
                        current.append(next)
                        index += 2
                        continue
                    }
                }
                if (ch == SINGLE_QUOTE || ch == DOUBLE_QUOTE) {
                    inQuote = ch
                    index++
                    continue
                }
                if (ch == ARG_SEPARATOR) {
                    result.add(current.toString())
                    current.setLength(0)
                    index++
                    continue
                }
                current.append(ch)
                index++
            }
            if (inQuote.code != 0) {
                return null
            }
            result.add(current.toString())
            return result
        }

        private fun skipWhitespaceForward(source: String, start: Int, endExclusive: Int): Int {
            var index = start
            while (index < endExclusive && source[index].isWhitespace()) {
                index++
            }
            return index
        }

        private fun skipWhitespaceBack(source: String, start: Int, endExclusive: Int): Int {
            var index = endExclusive - 1
            while (index >= start && source[index].isWhitespace()) {
                index--
            }
            return index
        }

        private fun asciiLowercase(value: String): String {
            var needsCopy = false
            for (ch in value) {
                if (ch in 'A'..'Z') {
                    needsCopy = true
                    break
                }
            }
            if (!needsCopy) {
                return value
            }
            val chars = CharArray(value.length)
            for (i in value.indices) {
                val ch = value[i]
                chars[i] = if (ch in 'A'..'Z') (ch.code + 32).toChar() else ch
            }
            return String(chars)
        }

        private fun handleTag(token: TagToken): Boolean {
            var rawName = token.name
            var negated = false
            if (!token.isClosing && rawName.startsWith("!")) {
                negated = true
                rawName = rawName.substring(1)
            }
            val rawLower = rawName
            if (token.isClosing) {
                closeTag(rawLower)
                return true
            }
            val custom = resolveCustom(rawLower, token.args)
            if (custom != null) {
                return handleCustomTag(rawLower, custom, token)
            }
            val name = normalizeTagName(rawLower)
            when (name) {
                "reset" -> {
                    reset()
                    return true
                }
                "newline" -> {
                    frames.last().children.add(Component.newline())
                    return true
                }
                "selector" -> {
                    val component = parseSelector(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "score" -> {
                    val component = parseScore(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "nbt" -> {
                    val component = parseNbt(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "key" -> {
                    val component = parseKeybind(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "lang" -> {
                    val component = parseTranslatable(token.args, null) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "lang_or" -> {
                    val component = parseTranslatable(token.args, token.args.getOrNull(1)) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "sprite" -> {
                    val component = parseSprite(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
                "head" -> {
                    val component = parseHead(token.args) ?: return false
                    frames.last().children.add(component)
                    return true
                }
            }

            when (name) {
                "color" -> {
                    val colorName = token.args.getOrNull(0) ?: return false
                    val color = parseTextColor(colorName) ?: return false
                    val style = Style.style().color(color).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "shadow" -> {
                    val shadow = parseShadowColor(token.args, negated) ?: return false
                    val style = Style.style().shadowColor(shadow).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "font" -> {
                    val fontArg = token.args.getOrNull(0) ?: return false
                    val key = parseKey(fontArg) ?: return false
                    val style = Style.style().font(key).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "click" -> {
                    val event = parseClick(token.args) ?: return false
                    val style = Style.style().clickEvent(event).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "hover" -> {
                    val event = parseHover(token.args) ?: return false
                    val style = Style.style().hoverEvent(event).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "insert" -> {
                    val insertion = token.args.getOrNull(0) ?: return false
                    val style = Style.style().insertion(insertion).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "bold", "italic", "underlined", "strikethrough", "obfuscated" -> {
                    val decoration = DECORATIONS[name] ?: return false
                    val disabled = negated || token.args.firstOrNull()?.equals("false", ignoreCase = true) == true
                    val state = if (disabled) TextDecoration.State.FALSE else TextDecoration.State.TRUE
                    val style = Style.style().decoration(decoration, state).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "rainbow" -> {
                    val rainbow = parseRainbow(token.args) ?: return false
                    if (!token.isSelfClosing) {
                        pushFrame(ColorFrame(name, rainbow))
                    }
                    return true
                }
                "gradient" -> {
                    val gradient = parseGradient(token.args) ?: return false
                    if (!token.isSelfClosing) {
                        pushFrame(ColorFrame(name, gradient))
                    }
                    return true
                }
                "transition" -> {
                    val color = parseTransitionColor(token.args) ?: return false
                    val style = Style.style().color(color).build()
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", style))
                    } else {
                        pushStyle(name, style)
                    }
                    return true
                }
                "pride" -> {
                    val pride = parsePride(token.args) ?: return false
                    if (!token.isSelfClosing) {
                        pushFrame(ColorFrame(name, pride))
                    }
                    return true
                }
            }

            val color = parseTextColor(name)
            if (color != null) {
                val style = Style.style().color(color).build()
                if (token.isSelfClosing) {
                    frames.last().children.add(Component.text("", style))
                } else {
                    pushStyle(name, style)
                }
                return true
            }

            return false
        }
        private fun resolveCustom(name: String, args: List<String>): MMTag? {
            if (resolver === MMTagResolver.empty()) {
                return null
            }
            if (!resolver.has(name)) {
                return null
            }
            return resolver.resolve(name, args, context)
        }

        private fun handleCustomTag(name: String, tag: MMTag, token: TagToken): Boolean {
            return when (tag) {
                is MMTag.PreProcess -> {
                    parseSegment(tag.value)
                    true
                }
                is MMTag.Styling -> {
                    if (token.isSelfClosing) {
                        frames.last().children.add(Component.text("", tag.style))
                    } else {
                        pushStyle(name, tag.style)
                    }
                    true
                }
                is MMTag.Inserting -> {
                    if (!token.isSelfClosing && tag.allowsChildren) {
                        pushFrame(InsertFrame(name, tag.component))
                    } else {
                        frames.last().children.add(tag.component)
                    }
                    true
                }
            }
        }

        private fun closeTag(name: String) {
            if (tryClose(name)) {
                return
            }
            val normalized = normalizeTagName(name)
            if (normalized != name) {
                tryClose(normalized)
            }
        }

        private fun tryClose(name: String): Boolean {
            var index = frames.size - 1
            while (index > 0) {
                val frame = frames[index]
                if (frame.tagName == name) {
                    while (frames.size - 1 >= index) {
                        closeTop()
                    }
                    return true
                }
                index--
            }
            return false
        }

        private fun reset() {
            while (frames.size > 1) {
                closeTop()
            }
        }

        private fun closeTop() {
            val top = frames.removeAt(frames.size - 1)
            frames.last().children.add(top.build())
        }

        private fun pushStyle(tagName: String, style: Style) {
            pushFrame(StyleFrame(tagName, style))
        }

        private fun pushFrame(frame: Frame) {
            frames.add(frame)
        }

        private fun parseClick(args: List<String>): ClickEvent? {
            if (args.size < 2) {
                return null
            }
            val actionName = asciiLowercase(args[0])
            val action = CLICK_ACTIONS[actionName] ?: return null
            val value = args[1]
            return when (action) {
                ClickEvent.Action.OPEN_URL -> ClickEvent.openUrl(value)
                ClickEvent.Action.OPEN_FILE -> ClickEvent.openFile(value)
                ClickEvent.Action.RUN_COMMAND -> ClickEvent.runCommand(value)
                ClickEvent.Action.SUGGEST_COMMAND -> ClickEvent.suggestCommand(value)
                ClickEvent.Action.CHANGE_PAGE -> {
                    val page = value.toIntOrNull() ?: return null
                    ClickEvent.changePage(page)
                }
                ClickEvent.Action.COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(value)
                ClickEvent.Action.SHOW_DIALOG -> null
                ClickEvent.Action.CUSTOM -> null
            }
        }

        private fun parseHover(args: List<String>): HoverEvent<*>? {
            if (args.size < 2) {
                return null
            }
            val action = asciiLowercase(args[0])
            return when (action) {
                "show_text" -> HoverEvent.showText(deserializeChild(args[1]))
                "show_item" -> {
                    val key = parseKey(args[1]) ?: return null
                    val count = args.getOrNull(2)?.toIntOrNull() ?: 1
                    if (args.size <= 3) {
                        return HoverEvent.showItem(key, count)
                    }
                    val dataComponents = parseDataComponents(args.subList(3, args.size)) ?: return null
                    if (dataComponents.isEmpty()) {
                        return HoverEvent.showItem(key, count)
                    }
                    HoverEvent.showItem(HoverEvent.ShowItem.showItem(key, count, dataComponents))
                }
                "show_entity" -> {
                    if (args.size < 3) {
                        return null
                    }
                    val type = parseKey(args[1]) ?: return null
                    val uuid = runCatching { UUID.fromString(args[2]) }.getOrNull() ?: return null
                    val name = args.getOrNull(3)?.let { deserializeChild(it) }
                    if (name != null) {
                        HoverEvent.showEntity(type, uuid, name)
                    } else {
                        HoverEvent.showEntity(type, uuid)
                    }
                }
                else -> null
            }
        }

        private fun parseSelector(args: List<String>): Component? {
            if (args.isEmpty()) {
                return null
            }
            val selector = args[0]
            val separator = args.getOrNull(1)?.let { deserializeChild(it) }
            return if (separator != null) {
                Component.selector(selector, separator)
            } else {
                Component.selector(selector)
            }
        }

        private fun parseScore(args: List<String>): Component? {
            if (args.size < 2) {
                return null
            }
            return Component.score(args[0], args[1])
        }

        private fun parseKeybind(args: List<String>): Component? {
            if (args.isEmpty()) {
                return null
            }
            return Component.keybind(args[0])
        }

        private fun parseTranslatable(args: List<String>, fallback: String?): Component? {
            if (args.isEmpty()) {
                return null
            }
            val key = args[0]
            val start = if (fallback == null) 1 else 2
            val components = ArrayList<Component>(maxOf(0, args.size - start))
            for (index in start until args.size) {
                components.add(deserializeChild(args[index]))
            }
            return if (fallback == null) {
                if (components.isEmpty()) Component.translatable(key) else Component.translatable(key, components)
            } else {
                if (components.isEmpty()) Component.translatable(key, fallback) else Component.translatable(key, fallback, components)
            }
        }

        private fun parseNbt(args: List<String>): Component? {
            if (args.size < 3) {
                return null
            }
            val type = asciiLowercase(args[0])
            var interpret = false
            var endIndex = args.size
            if (endIndex > 3 && args[endIndex - 1].equals("interpret", ignoreCase = true)) {
                interpret = true
                endIndex--
            }
            val separatorArg = if (endIndex > 3) args[3] else null
            val separator = separatorArg?.let { deserializeChild(it) }
            return when (type) {
                "block" -> {
                    val pos = runCatching { BlockNBTComponent.Pos.fromString(args[1]) }.getOrNull() ?: return null
                    Component.blockNBT { builder ->
                        builder.nbtPath(args[2])
                        builder.pos(pos)
                        builder.interpret(interpret)
                        if (separator != null) {
                            builder.separator(separator)
                        }
                    }
                }
                "entity" -> {
                    Component.entityNBT { builder ->
                        builder.nbtPath(args[2])
                        builder.selector(args[1])
                        builder.interpret(interpret)
                        if (separator != null) {
                            builder.separator(separator)
                        }
                    }
                }
                "storage" -> {
                    val key = parseKey(args[1]) ?: return null
                    Component.storageNBT { builder ->
                        builder.nbtPath(args[2])
                        builder.storage(key)
                        builder.interpret(interpret)
                        if (separator != null) {
                            builder.separator(separator)
                        }
                    }
                }
                else -> null
            }
        }

        private fun parseSprite(args: List<String>): Component? {
            if (args.isEmpty()) {
                return null
            }
            val contents = if (args.size == 1) {
                val spriteKey = parseKey(args[0]) ?: return null
                ObjectContents.sprite(spriteKey)
            } else {
                val atlasKey = parseKey(args[0]) ?: return null
                val spriteKey = parseKey(args[1]) ?: return null
                ObjectContents.sprite(atlasKey, spriteKey)
            }
            return Component.`object` { builder -> builder.contents(contents) }
        }

        private fun parseHead(args: List<String>): Component? {
            if (args.isEmpty()) {
                return null
            }
            val outerLayer = args.getOrNull(1)?.equals("false", ignoreCase = true) != true
            val input = args[0]
            val contents = when {
                isUuid(input) -> ObjectContents.playerHead(UUID.fromString(input))
                input.contains("/") || input.contains(":") -> {
                    val textureKey = parseKey(input) ?: return null
                    ObjectContents.playerHead(PlayerHeadObjectContents.SkinSource { builder ->
                        builder.texture(textureKey)
                    })
                }
                else -> ObjectContents.playerHead(input)
            }
            val finalContents = if (outerLayer) contents else contents.toBuilder().hat(false).build()
            return Component.`object` { builder -> builder.contents(finalContents) }
        }

        private fun parseRainbow(args: List<String>): Colorizer? {
            var reversed = false
            var phase = 0
            if (args.isNotEmpty()) {
                var value = args[0]
                if (value.startsWith("!")) {
                    reversed = true
                    value = value.substring(1)
                }
                if (value.isNotEmpty()) {
                    phase = value.toIntOrNull() ?: return null
                }
            }
            return RainbowColorizer(reversed, phase)
        }

        private fun parseGradient(args: List<String>): Colorizer? {
            val result = parseColorListWithPhase(args) ?: return null
            return GradientColorizer(result.colors, result.phase)
        }

        private fun parseTransitionColor(args: List<String>): TextColor? {
            val result = parseColorListWithPhase(args) ?: return null
            return transitionColor(result.colors, result.phase)
        }

        private fun parsePride(args: List<String>): Colorizer? {
            var flag = "pride"
            var phase = 0.0
            if (args.isNotEmpty()) {
                val first = asciiLowercase(args[0])
                if (PRIDE_FLAGS.containsKey(first)) {
                    flag = first
                    if (args.size > 1) {
                        phase = args[1].toDoubleOrNull() ?: return null
                        if (phase < -1.0 || phase > 1.0) {
                            return null
                        }
                    }
                } else if (first.isNotEmpty()) {
                    phase = first.toDoubleOrNull() ?: return null
                    if (phase < -1.0 || phase > 1.0) {
                        return null
                    }
                }
            }
            val colors = PRIDE_FLAGS[flag] ?: return null
            return GradientColorizer(colors.toTypedArray(), phase)
        }

        private fun parseColorListWithPhase(args: List<String>): ColorListResult? {
            if (args.isEmpty()) {
                return ColorListResult(DEFAULT_GRADIENT, 0.0)
            }
            val colors = ArrayList<TextColor>(args.size)
            var phase = 0.0
            var index = 0
            while (index < args.size) {
                val value = args[index]
                val color = parseTextColor(value)
                if (color != null) {
                    colors.add(color)
                    index++
                    continue
                }
                if (index == args.size - 1) {
                    val possiblePhase = value.toDoubleOrNull() ?: return null
                    if (possiblePhase < -1.0 || possiblePhase > 1.0) {
                        return null
                    }
                    phase = possiblePhase
                    index++
                    break
                }
                return null
            }
            if (colors.size == 1) {
                return null
            }
            if (colors.isEmpty()) {
                colors.addAll(DEFAULT_GRADIENT.toList())
            }
            return ColorListResult(colors.toTypedArray(), phase)
        }

        private fun parseShadowColor(args: List<String>, negated: Boolean): ShadowColor? {
            if (negated || args.firstOrNull()?.equals("false", ignoreCase = true) == true) {
                return ShadowColor.shadowColor(0x00000000)
            }
            if (args.isEmpty()) {
                val alpha = clampShadowAlpha(DEFAULT_SHADOW_ALPHA)
                return ShadowColor.shadowColor(alpha shl 24)
            }
            if (args.size == 1) {
                val alphaOnly = args[0].toFloatOrNull()
                if (alphaOnly != null) {
                    val alpha = clampShadowAlpha(alphaOnly)
                    return ShadowColor.shadowColor(alpha shl 24)
                }
            }
            val colorName = args[0]
            val alphaValue = args.getOrNull(1)?.toFloatOrNull()
            val hex = parseShadowHex(colorName, alphaValue)
            if (hex != null) {
                return ShadowColor.shadowColor(hex)
            }
            val color = parseTextColor(colorName) ?: return null
            val alpha = clampShadowAlpha(alphaValue ?: DEFAULT_SHADOW_ALPHA)
            return ShadowColor.shadowColor((alpha shl 24) or (color.value() and 0x00ffffff))
        }

        private fun parseShadowHex(value: String, alphaValue: Float?): Int? {
            if (value.length != 7 && value.length != 9) {
                return null
            }
            if (value[0] != '#') {
                return null
            }
            val rgb = parseHex(value, 1, 6) ?: return null
            if (value.length == 9) {
                val alpha = parseHex(value, 7, 2) ?: return null
                return (alpha shl 24) or rgb
            }
            val alpha = clampShadowAlpha(alphaValue ?: DEFAULT_SHADOW_ALPHA)
            return (alpha shl 24) or rgb
        }

        private fun parseHex(value: String, start: Int, count: Int): Int? {
            var result = 0
            val end = start + count
            var index = start
            while (index < end) {
                val digit = hexValue(value[index]) ?: return null
                result = (result shl 4) or digit
                index++
            }
            return result
        }

        private fun clampShadowAlpha(alpha: Float): Int {
            val clamped = alpha.coerceIn(0f, 1f)
            return (clamped * 255f + 0.5f).toInt()
        }

        private fun parseKey(value: String): Key? {
            val direct = runCatching { Key.key(value) }.getOrNull()
            if (direct != null) {
                return direct
            }
            if (value.contains(":")) {
                return null
            }
            return runCatching { Key.key("minecraft", value) }.getOrNull()
        }

        private fun parseTextColor(value: String): TextColor? {
            if (value.isEmpty()) {
                return null
            }
            if (value[0] == '#') {
                return parseHexColor(value)
            }
            val lower = asciiLowercase(value)
            return COLOR_ALIASES[lower] ?: NamedTextColor.NAMES.value(lower)
        }

        private fun parseHexColor(value: String): TextColor? {
            if (value.length != 7 || value[0] != '#') {
                return null
            }
            val rgb = parseHex(value, 1, 6) ?: return null
            return TextColor.color(rgb)
        }

        private fun deserializeChild(value: String): Component {
            val parsed = deserialize(value, resolver, pointered, dataComponentResolver)
            return compactChild(parsed)
        }

        private fun parseDataComponents(args: List<String>): Map<Key, DataComponentValue>? {
            if (args.isEmpty()) {
                return emptyMap()
            }
            if (dataComponentResolver === MMDataComponentResolver.empty()) {
                return null
            }
            val result = LinkedHashMap<Key, DataComponentValue>(args.size)
            for (entry in args) {
                val splitIndex = entry.indexOf('=')
                if (splitIndex <= 0 || splitIndex == entry.lastIndex) {
                    return null
                }
                val keyText = entry.substring(0, splitIndex)
                val valueText = entry.substring(splitIndex + 1)
                val key = parseKey(keyText) ?: return null
                val value = dataComponentResolver.resolve(key, valueText, context) ?: return null
                result[key] = value
            }
            return result
        }

        private fun compactChild(component: Component): Component {
            if (component !is TextComponent) {
                return component
            }
            if (component.content().isNotEmpty()) {
                return component
            }
            val children = component.children()
            if (children.size != 1) {
                return component
            }
            val style = component.style()
            if (style.isEmpty()) {
                return component
            }
            val child = children[0]
            val merged = child.style().merge(style, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)
            return child.style(merged)
        }

        private fun isUuid(value: String): Boolean {
            return runCatching { UUID.fromString(value) }.isSuccess
        }
    }
    private data class TagToken(
        val rawStart: Int,
        val rawEnd: Int,
        val name: String,
        val args: List<String>,
        val isClosing: Boolean,
        val isSelfClosing: Boolean,
        val endIndex: Int
    )

    private sealed class Frame(val tagName: String?) {
        val children = ArrayList<Component>(4)
        abstract fun build(): Component
    }

    private class RootFrame : Frame(null) {
        override fun build(): Component = combine(children)
    }

    private class StyleFrame(tagName: String, private val style: Style) : Frame(tagName) {
        override fun build(): Component {
            if (children.isEmpty()) {
                return Component.text("", style)
            }
            val builder = Component.text().style(style)
            for (child in children) {
                builder.append(child)
            }
            return builder.build()
        }
    }

    private class InsertFrame(tagName: String, private val base: Component) : Frame(tagName) {
        override fun build(): Component {
            if (children.isEmpty()) {
                return base
            }
            return base.append(children)
        }
    }

    private class ColorFrame(tagName: String, private val colorizer: Colorizer) : Frame(tagName) {
        override fun build(): Component {
            val combined = combine(children)
            val size = ColorTransform.sizeOf(combined)
            if (size <= 0) {
                return combined
            }
            colorizer.init(size)
            return ColorTransform.apply(combined, colorizer)
        }
    }

    private interface Colorizer {
        fun init(size: Int)
        fun color(): TextColor
        fun advance()
    }

    private class RainbowColorizer(
        private val reversed: Boolean,
        phase: Int
    ) : Colorizer {
        private var size = 0
        private var index = 0
        private val dividedPhase = phase / 10.0

        override fun init(size: Int) {
            this.size = size
            this.index = if (reversed) size - 1 else 0
        }

        override fun color(): TextColor {
            if (size <= 0) {
                return TextColor.color(0xffffff)
            }
            val hue = ((index.toDouble() / size) + dividedPhase) % 1.0
            return TextColor.color(HSVLike.hsvLike(hue.toFloat(), 1f, 1f))
        }

        override fun advance() {
            if (size <= 0) {
                return
            }
            if (reversed) {
                index = if (index == 0) size - 1 else index - 1
            } else {
                index++
            }
        }
    }

    private class GradientColorizer(colors: Array<TextColor>, phase: Double) : Colorizer {
        private val colors: Array<TextColor>
        private var phase = 0.0
        private var multiplier = 1.0
        private var index = 0

        init {
            if (phase < 0) {
                this.colors = colors.reversedArray()
                this.phase = 1 + phase
            } else {
                this.colors = colors
                this.phase = phase
            }
        }

        override fun init(size: Int) {
            multiplier = if (size <= 1) 0.0 else (colors.size - 1).toDouble() / (size - 1)
            phase *= colors.size - 1
            index = 0
        }

        override fun color(): TextColor {
            val position = (index * multiplier) + phase
            val lowUnclamped = floor(position).toInt()
            val high = ceil(position).toInt() % colors.size
            val low = lowUnclamped % colors.size
            return TextColor.lerp((position - lowUnclamped).toFloat(), colors[low], colors[high])
        }

        override fun advance() {
            index++
        }
    }

    private object ColorTransform {
        fun sizeOf(component: Component): Int {
            var size = 0
            traverse(component) { current ->
                if (current is TextComponent) {
                    val content = current.content()
                    if (content.isNotEmpty()) {
                        size += content.codePointCount(0, content.length)
                    }
                } else {
                    size += 1
                }
            }
            return size
        }

        fun apply(component: Component, colorizer: Colorizer): Component {
            return applyInternal(component, colorizer)
        }

        private fun applyInternal(component: Component, colorizer: Colorizer): Component {
            if (component.style().color() != null) {
                if (component is TextComponent) {
                    val content = component.content()
                    if (content.isNotEmpty()) {
                        advanceBy(content, colorizer)
                    }
                }
                return component
            }
            return when (component) {
                is TextComponent -> applyToText(component, colorizer)
                else -> applyToOther(component, colorizer)
            }
        }

        private fun applyToText(component: TextComponent, colorizer: Colorizer): Component {
            val content = component.content()
            val children = component.children()
            if (content.isEmpty() && children.isEmpty()) {
                return component
            }
            val builder = Component.text()
            if (content.isNotEmpty()) {
                val style = component.style()
                val holder = IntArray(1)
                val iterator = content.codePoints().iterator()
                while (iterator.hasNext()) {
                    holder[0] = iterator.nextInt()
                    builder.append(Component.text(String(holder, 0, 1), style.color(colorizer.color())))
                    colorizer.advance()
                }
            }
            if (children.isNotEmpty()) {
                for (child in children) {
                    builder.append(applyInternal(child, colorizer))
                }
            }
            return builder.build()
        }

        private fun applyToOther(component: Component, colorizer: Colorizer): Component {
            val colored = component.colorIfAbsent(colorizer.color())
            colorizer.advance()
            val children = component.children()
            if (children.isEmpty()) {
                return colored
            }
            val updatedChildren = ArrayList<Component>(children.size)
            for (child in children) {
                updatedChildren.add(applyInternal(child, colorizer))
            }
            return colored.children(updatedChildren)
        }

        private fun traverse(component: Component, visitor: (Component) -> Unit) {
            visitor(component)
            for (child in component.children()) {
                traverse(child, visitor)
            }
        }

        private fun advanceBy(content: String, colorizer: Colorizer) {
            val count = content.codePointCount(0, content.length)
            repeat(count) {
                colorizer.advance()
            }
        }
    }

    private data class ColorListResult(val colors: Array<TextColor>, val phase: Double)

    private fun transitionColor(colors: Array<TextColor>, phase: Double): TextColor {
        var adjustedPhase = phase.toFloat()
        var reversed = false
        val list = colors.toMutableList()
        if (adjustedPhase < 0f) {
            reversed = true
            adjustedPhase += 1f
            list.reverse()
        }
        val values = list.toTypedArray()
        val steps = 1f / (values.size - 1)
        for (index in 1 until values.size) {
            val point = index * steps
            if (point >= adjustedPhase) {
                val factor = 1 + (adjustedPhase - point) * (values.size - 1)
                return if (reversed) {
                    TextColor.lerp(1 - factor, values[index], values[index - 1])
                } else {
                    TextColor.lerp(factor, values[index - 1], values[index])
                }
            }
        }
        return values[0]
    }

    private fun combine(children: List<Component>): Component {
        return when (children.size) {
            0 -> Component.empty()
            1 -> {
                val child = children[0]
                if (child is TextComponent &&
                    child.content().isEmpty() &&
                    child.children().isNotEmpty() &&
                    child.style().isEmpty()
                ) {
                    Component.text()
                        .append(COMPACT_SENTINEL)
                        .append(child)
                        .build()
                } else {
                    child
                }
            }
            else -> Component.empty().append(children)
        }
    }

    private fun normalizeTagName(name: String): String {
        return when (name) {
            "colour", "c" -> "color"
            "b" -> "bold"
            "em", "i" -> "italic"
            "u" -> "underlined"
            "st" -> "strikethrough"
            "obf" -> "obfuscated"
            "br" -> "newline"
            "sel" -> "selector"
            "tr", "translate" -> "lang"
            "tr_or", "translate_or" -> "lang_or"
            "data" -> "nbt"
            "keybind" -> "key"
            "insertion" -> "insert"
            else -> name
        }
    }

    private fun hexValue(ch: Char): Int? {
        return when (ch) {
            in '0'..'9' -> ch.code - '0'.code
            in 'a'..'f' -> ch.code - 'a'.code + 10
            in 'A'..'F' -> ch.code - 'A'.code + 10
            else -> null
        }
    }
    private val DECORATIONS = mapOf(
        "bold" to TextDecoration.BOLD,
        "italic" to TextDecoration.ITALIC,
        "underlined" to TextDecoration.UNDERLINED,
        "strikethrough" to TextDecoration.STRIKETHROUGH,
        "obfuscated" to TextDecoration.OBFUSCATED
    )

    private val COLOR_ALIASES = mapOf(
        "dark_grey" to NamedTextColor.DARK_GRAY,
        "grey" to NamedTextColor.GRAY
    )

    private val CLICK_ACTIONS = ClickEvent.Action.entries.associateBy { it.name.lowercase(Locale.ROOT) }

    private val COMPACT_SENTINEL = Component.text(
        "",
        Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build()
    )

    private val DEFAULT_GRADIENT = arrayOf(
        TextColor.color(0xffffff),
        TextColor.color(0x000000)
    )

    private val PRIDE_FLAGS = mapOf(
        "pride" to listOf(0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
        "progress" to listOf(0xFFFFFF, 0xFFAFC7, 0x73D7EE, 0x613915, 0x000000, 0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
        "trans" to listOf(0x5BCFFB, 0xF5ABB9, 0xFFFFFF, 0xF5ABB9, 0x5BCFFB),
        "bi" to listOf(0xD60270, 0x9B4F96, 0x0038A8),
        "pan" to listOf(0xFF1C8D, 0xFFD700, 0x1AB3FF),
        "nb" to listOf(0xFCF431, 0xFCFCFC, 0x9D59D2, 0x282828),
        "lesbian" to listOf(0xD62800, 0xFF9B56, 0xFFFFFF, 0xD462A6, 0xA40062),
        "ace" to listOf(0x000000, 0xA4A4A4, 0xFFFFFF, 0x810081),
        "agender" to listOf(0x000000, 0xBABABA, 0xFFFFFF, 0xBAF484, 0xFFFFFF, 0xBABABA, 0x000000),
        "demisexual" to listOf(0x000000, 0xFFFFFF, 0x6E0071, 0xD3D3D3),
        "genderqueer" to listOf(0xB57FDD, 0xFFFFFF, 0x49821E),
        "genderfluid" to listOf(0xFE76A2, 0xFFFFFF, 0xBF12D7, 0x000000, 0x303CBE),
        "intersex" to listOf(0xFFD800, 0x7902AA, 0xFFD800),
        "aro" to listOf(0x3BA740, 0xA8D47A, 0xFFFFFF, 0xABABAB, 0x000000),
        "femboy" to listOf(0xD260A5, 0xE4AFCD, 0xFEFEFE, 0x57CEF8, 0xFEFEFE, 0xE4AFCD, 0xD260A5),
        "baker" to listOf(0xCD66FF, 0xFF6599, 0xFE0000, 0xFE9900, 0xFFFF01, 0x009900, 0x0099CB, 0x350099, 0x990099),
        "philly" to listOf(0x000000, 0x784F17, 0xFE0000, 0xFD8C00, 0xFFE500, 0x119F0B, 0x0644B3, 0xC22EDC),
        "queer" to listOf(0x000000, 0x9AD9EA, 0x00A3E8, 0xB5E51D, 0xFFFFFF, 0xFFC90D, 0xFC6667, 0xFEAEC9, 0x000000),
        "gay" to listOf(0x078E70, 0x26CEAA, 0x98E8C1, 0xFFFFFF, 0x7BADE2, 0x5049CB, 0x3D1A78),
        "bigender" to listOf(0xC479A0, 0xECA6CB, 0xD5C7E8, 0xFFFFFF, 0xD5C7E8, 0x9AC7E8, 0x6C83CF),
        "demigender" to listOf(0x7F7F7F, 0xC3C3C3, 0xFBFF74, 0xFFFFFF, 0xFBFF74, 0xC3C3C3, 0x7F7F7F)
    ).mapValues { (_, colors) -> colors.map(TextColor::color) }

    private const val DEFAULT_SHADOW_ALPHA = 0.25f
    private const val TAG_OPEN = '<'
    private const val TAG_CLOSE = '>'
    private const val ESCAPE = '\\'
    private const val SINGLE_QUOTE = '\''
    private const val DOUBLE_QUOTE = '"'
    private const val ARG_SEPARATOR = ':'
}
