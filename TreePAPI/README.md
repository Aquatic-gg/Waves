# TreePAPI 🌳

[![Code Quality](https://www.codefactor.io/repository/github/mrlarkyy/treepapi/badge)](https://www.codefactor.io/repository/github/mrlarkyy/treepapi)
[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/TreePAPI?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/TreePAPI)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-purple.svg?logo=kotlin)
[![Discord](https://img.shields.io/discord/884159187565826179?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

A high-performance, DSL-based library for creating complex, nested PlaceholderAPI expansions in Kotlin. Instead of messy `if-else` or `switch` chains, TreePAPI uses a tree structure to resolve placeholders with $O(1)$ literal lookups and efficient argument parsing.

## ✨ Features
*   **Intuitive DSL:** Define placeholders using a clean, nested structure.
*   **High Performance:** Uses HashMaps for literal lookups and index-based traversal to avoid unnecessary memory allocations.
*   **Smart Parsing:** Built-in support for quoted arguments (e.g., `%prefix_display_"My Name With Spaces"%`).
*   **Type Safe:** Easily extract arguments as `String`, `Int`, or Bukkit `Player` objects.
*   **Optional Arguments:** Fallback handlers allow for flexible placeholder depths (e.g., `%eco_bal%` and `%eco_bal_gems%`).

---

## 🛠️ Installation

Add the library to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("gg.aquatic:TreePAPI:26.0.1") // Replace with actual version
    compileOnly("me.clip:placeholderapi:2.11.7")
}
```

---

## 🚀 Usage

### 1. Define your Placeholders
Using the `placeholder` function, you can build your tree.

```kotlin
placeholder("YourName", "mystats") {
    
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
                    val target = argument<Player>("target")
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
}
```


### 2. Advanced Argument Parsing
The library automatically handles underscores within quotes, which is a common limitation in standard PAPI expansions.

| Placeholder                    | Tokens Resolved                |
|:-------------------------------|:-------------------------------|
| `%stats_kills_total%`          | `[stats, kills, total]`        |
| `%stats_"Vault_Money"_amount%` | `[stats, Vault_Money, amount]` |

---

## 💬 Community & Support

Got questions, need help, or want to showcase what you've built with **TreePAPI**? Join our community!

[![Discord Banner](https://img.shields.io/badge/Discord-Join%20our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/ffKAAQwNdC)

*   **Discord**: [Join the Aquatic Development Discord](https://discord.com/invite/ffKAAQwNdC)
*   **Issues**: Open a ticket on GitHub for bugs or feature requests.