package gg.aquatic.quickminimessage.parser

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
import java.util.UUID

internal class Parser(
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
            handleCustomTag(rawLower, custom, token)
            return true
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

    private fun handleCustomTag(name: String, tag: MMTag, token: TagToken) {
        when (tag) {
            is MMTag.PreProcess -> {
                parseSegment(tag.value)
            }
            is MMTag.Styling -> {
                if (token.isSelfClosing) {
                    frames.last().children.add(Component.text("", tag.style))
                } else {
                    pushStyle(name, tag.style)
                }
            }
            is MMTag.Inserting -> {
                if (!token.isSelfClosing && tag.allowsChildren) {
                    pushFrame(InsertFrame(name, tag.component))
                } else {
                    frames.last().children.add(tag.component)
                }
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
                ObjectContents.playerHead { builder ->
                    builder.texture(textureKey)
                }
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
        val parsed = MMParserEngine.deserialize(value, resolver, pointered, dataComponentResolver)
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

