# Zombie Maze

**Created by Ethan Rodrigues**

An object-oriented Java Swing game where the player navigates a dark maze, avoids enemies, collects keys, finds a cure, and heals every zombie before escaping. 

---

## Folder Structure

```
ZombieMaze/
├── Makefile
├── README.md
├── src/                              - all Java source files
│   ├── Main.java                           - entry point
│   ├── GamePanel.java                      - game loop, level management, input
│   ├── GameState.java                      - MENU / PLAYING / WIN / GAME_OVER
│   ├── LevelData.java                      - per-level config (walls, keys, zombies, background…)
│   ├── Levels.java                         - level index; routes level number to the right class
│   ├── Level1.java                         - Level 1 definition: 12×6 grid, large cells, dark green
│   ├── Level2.java                         - Level 2 definition: 17×9 grid, medium cells, dark red
│   ├── Level3.java                         - Level 3 definition: 20×11 grid, small cells, dark blue
│   ├── LevelFinal.java                     - Final level: same grid as Level 3, golden theme, chasing zombie
│   ├── CollisionChecker.java               - AABB wall + gate collision from LevelData.walls
│   ├── Player.java                         - movement, scale-aware sprite, invincibility
│   ├── Zombie.java                         - PATROL / CHASING / HUMAN modes, heal state
│   ├── Rock.java                           - falling rock hazard
│   ├── HUD.java                            - hearts, keys, live timer, level indicator, control legend
│   ├── MenuRenderer.java                   - all non-gameplay screens (menu, leaderboard, win, game over)
│   ├── Leaderboard.java                    - persistent top-times list with name entry
│   └── SpriteSheet.java                    - image loading / cropping / scaling utilities
│
├── assets/                             - game assets
│   ├── Background1-4.png
│   ├── RobotSpriteSheet.png
│   ├── ZombieSpriteSheet.png
│   ├── ZombieSpriteSheet2-4.png
│   ├── ZombieSpriteSheetFinal.png
│   ├── HumanSpriteSheet.png
│   ├── cure.png
│   ├── keyImage.png
│   ├── heart.png
│   ├── EmptyHeart.png
│   ├── flashlightCircleSmaller.png
│   ├── flashlightCircleSmaller.png
│   └── leaderboard.json                    - game leaderboard file (generated once items are added to the leaderboard)
│
└── bin/                                - compiled .class files (created by make)
```

---

## How to Run

### Requirements
- **Java JDK 11+**
  - macOS: `brew install openjdk`
  - Ubuntu: `sudo apt install default-jdk`
  - Windows: `winget install Microsoft.OpenJDK.21`

### Build & Run
```bash
cd ZombieMaze
make run
```

Clean compiled files:
```bash
make clean
```

---

## How to Play

| Input | Action |
|-------|--------|
| `W` / `↑` | Move up |
| `S` / `↓` | Move down |
| `A` / `←` | Move left |
| `D` / `→` | Move right |
| `Q` / `ESC` | Quit to main menu |

---

### The Full Game Loop

#### Phase 1 — Levels 1–3
- Collect **all keys** scattered through the maze to unlock the **exit gate** on the right
- Avoid **patrolling zombies** and **falling rocks** — each hit costs 1 life
- The maps for each level get bigger, making the player and their visible flashlight radius smaller each time 
- Lives carry over between levels, the player may collect lives that are scattered throughout the maze to recover

#### Phase 2 — Final Level (Find the Cure)
- A **chasing zombie** hunts you down
- Find the **CURE vial** hidden in the maze and collect it — this restores your health and transforms the zombie back into a human once you come into contact with them

#### Phase 3 — Heal-Back Traversal (Coming Back Out)
- Re-enter each previous level from the **right side**
- The **left entrance is locked** — it only opens once you have **touched and healed every zombie** on that level
- After healing all zombies on a level, the entrance gate opens and you proceed further left
- Heal all zombies across all levels and exit through **Level 1's left gate** to win

---

### HUD (In-Game Display)

| Element | Location | Description |
|---------|----------|-------------|
| Hearts | Top-left | Remaining lives |
| Timer | Top-left (below hearts) | Elapsed time, used on the leaderboard |
| Level badge | Top-right | Level number or "FINAL LEVEL" |
| Key icons | Bottom-right | Keys collected so far (Shown only on levels 1–3) |
| Cure status | Bottom-right | Heal progress on the final level |
| Control legend | Bottom strip | Always visible, indicates controls to the player |

---

### Win Screen & Leaderboard
- Your **total completion time** is shown on the win screen
- Your **leaderboard rank** is displayed before you submit your name
- Enter your name and press **ENTER** to save your time
- View the full leaderboard from the main menu at any time
