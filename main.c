#include <__APPLE__>
#include <METAL.h>
#include <Ne10.h>
#include <stdatomic.h>
#include <stdio.h>

typedef struct 
{
        atomic_int value; // Valeur atomique
} AtomicRef;

void atomic_ref_init(AtomicRef *ref, int initial)
{
        atomic_init(&ref->value, initial);
}

int atomic_ref_get(AtomicRef *ref)
{
        return atomic_load(&ref->value); // Lecture atomique
}

void atomic_ref_set(AtomicRef *ref, int new_value)
{
        atomic_store(&ref->value, new_value); // Écriture atomique
}

int atomic_ref_update(AtomicRef *ref, int (*update_func)(int))
{
        int old_value, new_value;
        do
        {
                old_value = atomic_load(&ref->value); // Étape 1 : Lecture
                
        } while (!atomic_compare_exchange_weak(&ref->value, &old_value, new_value)); // Étape 3 : CAS
        return new_value;
}

// Exemple d'utilisation
int increment(int x) { return x + 1; }

int main()
{
        AtomicRef ref;
        atomic_ref_init(&ref, 0);

        atomic_ref_update(&ref, increment);                            // Incrémente de manière atomique
        printf("Valeur après incrément : %d\n", atomic_ref_get(&ref)); // Devrait afficher 1

        return 0;
}

    dscdcsc