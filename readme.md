# ⚒️ MMO-Job Feature Plugin

A Highly configurable Minecraft job system for Paper 1.21+. This plugin features a robust leveling curve, interactive GUI menus, and a unique skill point system.

## ✨ Features

- **Dynamic Leveling Curve**: Configurable scaling XP requirements.
- **Skill Point System**: Earn points by leveling up to upgrade your job-specific abilities.
- **Interactive GUI**: Accessible via **Shift + F** (swap-hand shortcut).
- **Attribute Bonuses**: Jobs automatically grant stat boosts (e.g., +Max Health, +Attack Damage).
- **Modular YAML Configs**: Every job is a separate file, allowing for easy expansion and balancing.

## 🎮 How It Works

### 1. Joining a Job
Players can join a job via command. Some jobs have **requirements** (e.g., minimum level or specific permissions).
- **Command**: `/job set <jobname>`
- **Example**: `/job set Miner`

### 2. Gaining Experience (XP)
Perform actions mapped to your job to gain XP. Once your XP reaches the threshold, you level up!
- **Miner**: Mine ores, smelt materials, or break stone.
- **Beekeeper**: Harvest honey, breed bees, or craft beehives.
- **Endwalker**: Kill Endermen, use Ender Pearls, or loot End Cities.

### 3. Upgrading Skills
Every time you level up, you gain **Skill Points**. Press **Shift + F** to open your Skill Menu.
- **Left-Click**: Toggle a passive/active skill or trigger its effect.
- **Right-Click**: Spend 1-2 Skill Points to increase a skill's level (up to its maximum).

### 4. Attribute Bonuses
Simply being in a job grants you "Passive Bonuses".
- **Nethermancer**: Grants +2.0 Armor and +1.0 Armor Toughness.
- **Fisherman**: Grants +1.0 Luck and +0.02 Movement Speed.

## 🛠️ Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/job set <name>` | Select your current job | `job.use` |
| `/job stats` | View your level and skill points | `job.use` |
| `/job list` | List all available jobs | `job.use` |
| `/job reload` | Reload all YAML configs | `job.admin` |

## 📁 Configuration Structure

The plugin data folder is organized as follows:
- **`config.yml`**: Global settings like the XP level curve math and GUI shortcuts.
- **`/jobs/`**: Contains individual YAML files for each job (e.g., `Lumberjack.yml`).
- **`/playerdata/`**: Stores player progress, current job, and skill levels.

### Example Job Configuration (`jobs/Miner.yml`)
```yaml
name: Miner
bonuses:
  generic.max_health: 2.0
skills:
  Vein Master:
    type: Passive
    trigger_item: DIAMOND_ORE
    max_level: 5
    points_per_level: 1
    toggleable: true
rewards:
  Mine ores: 15
```

## 🚀 Installation & Building

1. Clone the repository into your development environment.
2. Build the plugin using Gradle:
   ```bash
   ./gradlew build
   ```
3. Copy the generated JAR from `build/libs/` to your server's `plugins/` folder.
4. Restart the server and enjoy!
