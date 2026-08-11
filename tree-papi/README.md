# TreePAPI

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/treepapi/badge)](https://www.codefactor.io/repository/github/mrlarkyy/treepapi)
[![Reposilite](https://repo.aquatic.gg/api/badge/latest/releases/gg/aquatic/tree-papi?color=40c14a&name=Reposilite)](https://repo.aquatic.gg/#/releases/gg/aquatic/tree-papi)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A DSL-based library for building nested PlaceholderAPI expansions in Kotlin. Instead of `if-else` or `when` chains,
TreePAPI resolves placeholders through a tree structure with O(1) literal lookups.

## Features
*   **DSL:** Define placeholders using a nested structure.
*   **Literal lookups:** Uses hash maps for literal lookups and index-based traversal to limit allocations.
*   **Quoted arguments:** Supports quoted arguments (e.g. `%prefix_display_"My Name With Spaces"%`).
*   **Type-safe:** Extract arguments as `String`, `Int`, or Bukkit `Player` objects.
*   **Optional arguments:** Fallback handlers allow flexible placeholder depths (e.g. `%eco_bal%` and `%eco_bal_gems%`).

---

## Installation

Add the library to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.aquatic.gg/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("gg.aquatic:tree-papi:26.0.54") // Replace with actual version
    compileOnly("me.clip:placeholderapi:2.11.7")
}
```

---

## Usage

### 1. Define your Placeholders
Using the `papiPlaceholder` function, you can build your tree.

```kotlin
papiPlaceholder("YourName", "mystats") {
    
    // Literal node: %mystats_kills%
    "kills" {
        handle { "1,250" }
    }

    // Nested Literals: %mystats_eco_balance%
    "eco" {
        "balance" {
            handle { "500.00" }
        }
    }

    // Arguments: %mystats_user_<name>_level%
    "user" {
        playerArgument("target") {
            "level" {
                handle {
                    val target = getOrNull<Player>("target")
                    target?.level?.toString() ?: "0"
                }
            }
        }
    }

    // Optional Arguments: %mystats_rank% vs %mystats_rank_global%
    "rank" {
        handle { "Pro" } // Default if no sub-placeholder is used
        
        "global" {
            handle { "#152" }
        }
    }

    // Optional Arguments: %mystats_wins% vs %mystats_wins_<player>%
    "wins" {
        // This single handler covers both cases!
        handle {
            val target = string("player") ?: binder.name
            getWins(target)
        }
        stringArgument("player") {
            // Falling back to parent handler automatically
        }
    }
}
```


### 2. Advanced Argument Parsing
The library automatically handles underscores within quotes, which is a common limitation in standard PAPI expansions.

| Placeholder                      | Tokens Resolved                  |
|:---------------------------------|:---------------------------------|
| `%mystats_kills_total%`          | `[mystats, kills, total]`        |
| `%mystats_"Vault_Money"_amount%` | `[mystats, Vault_Money, amount]` |

---

## Community & Support

- Discord: [Aquatic Development](https://discord.com/invite/ffKAAQwNdC)
- Issues: open a ticket on GitHub for bugs or feature requests.