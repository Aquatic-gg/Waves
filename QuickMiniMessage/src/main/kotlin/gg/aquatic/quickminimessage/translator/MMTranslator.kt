package gg.aquatic.quickminimessage.translator

import gg.aquatic.quickminimessage.MMLocalePointered
import gg.aquatic.quickminimessage.MMParser
import gg.aquatic.quickminimessage.tag.resolver.MMArgumentTagResolver
import gg.aquatic.quickminimessage.argument.MMTranslatorArgument
import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgumentLike
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.Locale

@Suppress("unused")
abstract class MMTranslator(
    private val baseResolver: MMTagResolver = MMTagResolver.empty()
) : Translator {
    protected abstract fun getMiniMessageString(key: String, locale: Locale): String?

    override fun translate(key: String, locale: Locale): MessageFormat? = null

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val miniMessage = getMiniMessageString(component.key(), locale) ?: return null
        val context = buildContext(component.arguments(), locale)
        val resolved = MMParser.deserialize(miniMessage, context.resolver, context.pointered)
        return applyStyleAndChildren(resolved, component)
    }

    private fun buildContext(
        arguments: List<TranslationArgument>,
        locale: Locale
    ): ResolverContext {
        if (arguments.isEmpty()) {
            return ResolverContext(baseResolver, MMLocalePointered(locale))
        }
        val builder = MMTagResolver.builder()
        val positional = ArrayList<MMTag>(arguments.size)
        val state = ResolverBuildState(MMLocalePointered(locale))

        for (argument in arguments) {
            if (!handleVirtualArgument(argument, builder, positional, state)) {
                positional.add(MMTag.selfClosingInserting(argument))
            }
        }

        val argumentResolver = MMArgumentTagResolver(positional, builder.build())
        val resolver = MMTagResolver.resolver(argumentResolver, baseResolver)
        return ResolverContext(resolver, state.pointered)
    }

    private fun handleVirtualArgument(
        argument: TranslationArgument,
        builder: MMTagResolver.Builder,
        positional: MutableList<MMTag>,
        state: ResolverBuildState
    ): Boolean {
        val value = argument.value()
        if (value !is VirtualComponent) {
            return false
        }
        when (val renderer = value.renderer()) {
            is MMTranslatorTarget -> {
                if (state.targetSet) {
                    throw IllegalArgumentException("Multiple Argument.target() translation arguments have been set!")
                }
                state.pointered = renderer.pointered
                state.targetSet = true
                return true
            }
            is MMTranslatorArgument<*> -> {
                when (val data = renderer.data) {
                    is TranslationArgumentLike -> {
                        val tag = MMTag.selfClosingInserting(data)
                        builder.tag(renderer.name, tag)
                        positional.add(tag)
                        return true
                    }
                    is MMTag -> {
                        builder.tag(renderer.name, data)
                        positional.add(data)
                        return true
                    }
                    is MMTagResolver -> {
                        builder.resolver(data)
                        return true
                    }
                    else -> {
                        val type = data?.javaClass?.name ?: "null"
                        throw IllegalArgumentException("Unknown translator argument type: $type")
                    }
                }
            }
        }
        return false
    }

    private fun applyStyleAndChildren(base: Component, component: TranslatableComponent): Component {
        var result = base
        val style = component.style()
        if (!style.isEmpty) {
            result = result.applyFallbackStyle(style)
        }
        val children = component.children()
        if (children.isNotEmpty()) {
            result = result.append(children)
        }
        return result
    }

    private data class ResolverContext(
        val resolver: MMTagResolver,
        val pointered: Pointered
    )

    private class ResolverBuildState(
        var pointered: Pointered,
        var targetSet: Boolean = false
    )
}
