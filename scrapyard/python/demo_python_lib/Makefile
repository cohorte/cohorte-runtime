################################################################################
## Test library for demo_python run                                           ##
################################################################################

# Compiler
CC=gcc
CFLAGS=-fPIC -c

# Output
OUT_LINK=libdemo_python_lib.so

OUT_1=libdemo_python_lib_1.so
OUT_2=libdemo_python_lib_2.so

ifneq ($(UPDATED), 1)
# Not the updated version
OUT=$(OUT_1)
else
# The update version
OUT=$(OUT_2)
CFLAGS+=-DUPDATED
endif

all: normal updated

# Compile
%.o: %.c
	@$(CC) $(CFLAGS) -o "$@" "$<"

$(OUT): lib.o
	@$(CC) -shared -o "$(OUT)" $<

# Other
build: $(OUT)

normal: clean build

updated:
	@make UPDATED=1 normal

clean:
	@rm -f *.o

mrproper: clean
	@rm -f "$(OUT_1)"
	@rm -f "$(OUT_2)"
	@rm -f "$(OUT_LINK)"

