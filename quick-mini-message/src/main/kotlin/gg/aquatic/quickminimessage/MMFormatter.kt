package gg.aquatic.quickminimessage

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.MMTagContext
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import java.text.ChoiceFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

@Suppress("unused")
object MMFormatter {
    @JvmStatic
    fun number(name: String, number: Number): MMTagResolver {
        return MMTagResolver.resolver(name) { args, context ->
            val format = parseNumberFormat(args)
            val formatted = format.format(number)
            MMTag.inserting(context.deserialize(formatted))
        }
    }

    @JvmStatic
    fun date(name: String, temporal: TemporalAccessor): MMTagResolver {
        return MMTagResolver.resolver(name) { args, context ->
            val pattern = args.firstOrNull() ?: return@resolver null
            val formatted = runCatching { DateTimeFormatter.ofPattern(pattern).format(temporal) }.getOrNull()
                ?: return@resolver null
            MMTag.inserting(context.deserialize(formatted))
        }
    }

    @JvmStatic
    fun choice(name: String, number: Number): MMTagResolver {
        return MMTagResolver.resolver(name) { args, context ->
            val pattern = args.firstOrNull() ?: return@resolver null
            val formatted = runCatching { ChoiceFormat(pattern).format(number) }.getOrNull()
                ?: return@resolver null
            MMTag.inserting(context.deserialize(formatted))
        }
    }

    @JvmStatic
    fun booleanChoice(name: String, value: Boolean): MMTagResolver {
        return MMTagResolver.resolver(name) { args, context ->
            val trueFormat = args.getOrNull(0) ?: return@resolver null
            val falseFormat = args.getOrNull(1) ?: return@resolver null
            val selected = if (value) trueFormat else falseFormat
            MMTag.inserting(context.deserialize(selected))
        }
    }

    @JvmStatic
    fun joining(name: String, components: Iterable<ComponentLike>): MMTagResolver {
        return MMTagResolver.resolver(name) { args, context ->
            val configuration = buildJoinConfiguration(args, context)
            val joined = Component.join(configuration, components)
            MMTag.inserting(joined)
        }
    }

    @JvmStatic
    fun joining(name: String, vararg components: ComponentLike): MMTagResolver {
        return joining(name, components.asList())
    }

    private fun parseNumberFormat(args: List<String>): NumberFormat {
        if (args.isEmpty()) {
            return DecimalFormat.getInstance()
        }
        val first = args[0]
        val second = args.getOrNull(1)
        return if (second != null) {
            DecimalFormat(second, DecimalFormatSymbols(Locale.forLanguageTag(first)))
        } else if (first.contains(".")) {
            DecimalFormat(first, DecimalFormatSymbols.getInstance())
        } else {
            DecimalFormat.getInstance(Locale.forLanguageTag(first))
        }
    }

    private fun buildJoinConfiguration(args: List<String>, context: MMTagContext): JoinConfiguration {
        if (args.isEmpty()) {
            return JoinConfiguration.noSeparators()
        }
        val builder = JoinConfiguration.builder()
            .separator(context.deserialize(args[0]))
        val lastSeparator = args.getOrNull(1)
        if (lastSeparator != null) {
            builder.lastSeparator(context.deserialize(lastSeparator))
        }
        val lastSeparatorIfSerial = args.getOrNull(2)
        if (lastSeparatorIfSerial != null) {
            builder.lastSeparatorIfSerial(context.deserialize(lastSeparatorIfSerial))
        }
        return builder.build()
    }
}
