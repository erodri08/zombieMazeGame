# Zombie Maze — Makefile


# NOTE: Requires Java JDK 11+  (javac + java)
#   macOS:   brew install openjdk
#   Ubuntu:  sudo apt install default-jdk
#   Windows: winget install Microsoft.OpenJDK.21

SRC_DIR = src
BIN_DIR = bin
ASSETS  = assets
SRCS    = $(wildcard $(SRC_DIR)/*.java)
MAIN    = Main

ifeq ($(OS),Windows_NT)
  SEP = ;
else
  SEP = :
endif

JAVAC := $(shell which javac 2>/dev/null)
JAVA  := $(shell which java  2>/dev/null)
ifeq ($(JAVAC),)
  $(error javac not found. Install JDK 11+ and add it to PATH.)
endif

.PHONY: all run clean

all: $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SRCS)
	@echo "Build complete. Run: make run"

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

run: all
	cd $(ASSETS) && $(JAVA) -cp "../$(BIN_DIR)" $(MAIN)

clean:
	rm -rf $(BIN_DIR)
