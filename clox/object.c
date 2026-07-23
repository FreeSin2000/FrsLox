#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType, arrayType, arraySize) \
    (type *)allocateObject(sizeof(type) + arraySize * sizeof(arrayType), objectType)

static Obj *allocateObject(size_t size, ObjType type)
{
    Obj *object = (Obj *)reallocate(NULL, 0, size);
    object->type = type;

    object->next = vm.objects;
    vm.objects = object;
    return object;
}

ObjString *copyString(const char *chars, int length)
{
    ObjString *string = ALLOCATE_OBJ(ObjString, OBJ_STRING, char, (length + 1));
    string->length = length;
    string->isConstant = false;
    memcpy(string->extra, chars, length);
    string->extra[length] = '\0';
    string->dynChars = string->extra;

    return string;
}

ObjString *constString(const char *chars, int length)
{
    ObjString *string = ALLOCATE_OBJ(ObjString, OBJ_STRING, char, 0);
    string->length = length;
    string->isConstant = true;
    string->constChars = chars;
    return string;
}

void printObject(Value value)
{
    switch (OBJ_TYPE(value))
    {
    case OBJ_STRING:
        printf("%.*s", AS_STRING(value)->length, AS_CSTRING(value));
        break;
    }
}