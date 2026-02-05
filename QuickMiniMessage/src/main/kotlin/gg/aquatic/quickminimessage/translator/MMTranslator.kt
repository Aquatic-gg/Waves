package gg.aquatic.quickminimessage.translator

import gg.aquatic.quickminimessage.MMLocalePointered
import gg.aquatic.quickminimessage.MMParser
import gg.aquatic.quickminimessage.tag.resolver.MMArgumentTagResolver
import gg.aquatic.quickminimessage.argument.MMTranslatorArgument
import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgumentLike
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.Locale

abstract class MMTranslator(
    private val baseResolver: MMTagResolver = MMTagResolver.empty()
) : Translator {
    protected abstract fun getMiniMessageString(key: String, locale: Locale): String?

    override fun translate(key: String, locale: Locale): MessageFormat? = null

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val miniMessage = getMiniMessageString(component.key(), locale) ?: return null
        var pointered: Pointered = MMLocalePointered(locale)
        val arguments = component.arguments()
        val resolved = if (arguments.isEmpty()) {
            MMParser.deserialize(miniMessage, baseResolver, pointered)
        } else {
            val builder = MMTagResolver.builder()
            val positional = ArrayList<MMTag>(arguments.size)
            var targetSet = false
            for (argument in arguments) {
                val value = argument.value()
                if (value is VirtualComponent) {
                    val renderer = value.renderer()
                    when (renderer) {
                        is MMTranslatorTarget -> {
                            if (targetSet) {
                                throw IllegalArgumentException("Multiple Argument.target() translation arguments have been set!")
                            }
                            pointered = renderer.pointered
                            targetSet = true
                            continue
                        }
                        is MMTranslatorArgument<*> -> {
                            val data = renderer.data
                            when (data) {
                                is TranslationArgumentLike -> {
                                    val tag = MMTag.selfClosingInserting(data)
                                    builder.tag(renderer.name, tag)
                                    positional.add(tag)
                                    continue
                                }
                                is MMTag -> {
                                    builder.tag(renderer.name, data)
                                    positional.add(data)
                                    continue
                                }
                                is MMTagResolver -> {
                                    builder.resolver(data)
                                }
                                else -> {
                                    val type = data?.javaClass?.name ?: "null"
                                    throw IllegalArgumentException("Unknown translator argument type: $type")
                                }
                            }
                        }
                    }
                }
                positional.add(MMTag.selfClosingInserting(argument))
            }
            val argumentResolver = MMArgumentTagResolver(positional, builder.build())
            val resolver = MMTagResolver.resolver(argumentResolver, baseResolver)
            MMParser.deserialize(miniMessage, resolver, pointered)
        }
        var result = resolved
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
}
