# Variables
JAVAC = javac
JAVA = java
SRC_DIR = .
BIN_DIR = bin
SRCS = $(shell find $(SRC_DIR) -name "*.java" -not -path "*/test/*" -not -path "*/tests/*")
CLASSES = $(SRCS:%.java=$(BIN_DIR)/%.class)
MAIN_CLASS = Main

# Default target
all: compile

# Create bin directory if it doesn't exist
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

# Compile all Java files (excluding tests)
compile: $(BIN_DIR) $(CLASSES)

# Pattern rule to compile Java files
$(BIN_DIR)/%.class: %.java
	@mkdir -p $(dir $@)
	$(JAVAC) -d $(BIN_DIR) $<

# Clean compiled files
clean:
	rm -rf $(BIN_DIR)

# Run the application with a username
run: compile
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS) -u $(USERNAME)

# Help target
help:
	@echo "Targets disponíveis:"
	@echo "  all (padrão): Compila o projeto"
	@echo "  compile: Compila o projeto"
	@echo "  clean: Remove os arquivos .class compilados"
	@echo "  run USERNAME=username: Executa a aplicação com o nome de usuário especificado"
	@echo "  help: Exibe esta mensagem de ajuda"

.PHONY: all compile clean run help
