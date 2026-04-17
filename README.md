# Zombie Maze

**Created by Ethan Rodrigues**

A Java/Processing maze game where you navigate a dark maze, collect keys, avoid zombies and falling rocks, and escape alive.

---

## Folder Structure

```
ZombieMaze/
├── Makefile                  ← build & run commands
├── README.md                 ← this file
│
├── src/                      ← all Java source files
│   ├── Main.java             ← entry point
│   ├── Game.java             ← main PApplet; orchestrates everything
│   ├── GameState.java        ← enum: MENU / INSTRUCTIONS / PLAYING / WIN / GAME_OVER
│   ├── LevelData.java        ← data class holding one level's configuration
│   ├── Levels.java           ← registry of all levels (add new levels here)
│   ├── Player.java           ← player sprite, movement, lives, invincibility
│   ├── Zombie.java           ← zombie patrol + animation
│   ├── Rock.java             ← falling rock hazard
│   ├── CollisionChecker.java ← all wall-collision logic for Level 1
│   ├── HUD.java              ← draws hearts and key icons
│   └── MenuRenderer.java     ← text-based menu / win / game-over / instructions screen
│
├── assets/                   ← all image files used at runtime
│   ├── Background.png
│   ├── RobotSpriteSheet.png
│   ├── ZombieSpriteSheet.png
│   ├── keyImage.png
│   ├── heart.png
│   ├── EmptyHeart.png
│   └── flashlightCircle2.png
│
└── bin/                      ← compiled .class files (created by make; git-ignored)
```

---

## How to Run

### Prerequisites

- **Java JDK 11 or later** — check with `java -version`
- **`make`** — ships with macOS/Linux; Windows users can install via [Chocolatey](https://chocolatey.org/) (`choco install make`) or use Git Bash

### Steps

```bash
# 1. Open a terminal in the ZombieMaze folder
cd ZombieMaze

# 2. Compile
make

# 3. Run
make run

# Or do both at once:
make run
```

To clean compiled files:

```bash
make clean
```

> **Note:** The game window must be launched from the `assets/` directory so that
> Processing can find the image files. The Makefile handles this automatically via
> `cd assets && java …`. Do **not** run `java` directly from the project root.

---

## How to Play

| Key | Action |
|-----|--------|
| `W` | Move up |
| `A` | Move left |
| `S` | Move down |
| `D` | Move right |

- **Collect 4 keys** scattered throughout the maze — each key you collect is shown in the lower-right corner.
- Collecting all 4 keys **opens the exit gate** on the right side of the screen.
- **Avoid 5 zombies** patrolling the corridors.
- **Avoid falling rocks** dropping from above.
- Contact with an enemy costs **1 of your 5 lives** (shown as hearts in the upper-left).
- Lose all 5 lives → **Game Over** screen appears.
- Reach the exit with all keys → **Win** screen appears.

### Known Limitation
Collision detection uses rectangular bounding boxes around sprite images. Because PNG images have transparent padding around the visible character, you may occasionally lose a life when the characters do not visually appear to touch. This is a known limitation of the current approach.

---

## Adding a New Level

1. Open `src/Levels.java`.
2. Write a new private static method `level2()` following the same pattern as `level1()` — define key positions, zombie patrols, and wall rectangles.
3. Add `level2()` to the `LEVELS` array.
4. In `Game.java`, after a win you can call `loadLevel(1)` instead of resetting to level 0.

All level-specific data (walls, key positions, zombie patrol areas, gate positions) is isolated in `LevelData` and `Levels.java`, so the rest of the code needs no changes.
