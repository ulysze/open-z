#include <__APPLE__>
#include <METAL.h>
#include <Ne10.h>
#include <stdatomic.h>
#include <stdio.h>

int compare_and_swap(int* adress, expected, new_value){
        old_value = *adress
        if (old_value == expected) 
                *adress = new_value;
        return old_value;
}