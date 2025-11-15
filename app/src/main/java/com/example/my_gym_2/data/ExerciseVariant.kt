package com.example.my_gym_2.data

// Put near your puchWorkoutList/pullWorkoutList/legWorkoutList
data class ExerciseVariant(val name: String, val image: String)

val exerciseVariants: Map<String, List<ExerciseVariant>> = mapOf(
    "Bench Press" to listOf(
        ExerciseVariant("Bench Press", "bench_press.jpg"),
        ExerciseVariant("Bench Press (Dumbbell)", "bench_press_dumbbell.jpg")
    ),
    // add others as you like, e.g. Shoulder Press, Row, etc.
    // "Shoulder Press" to listOf(
    //     ExerciseVariant("Shoulder Press (Barbell)", "shoulder_press.jpg"),
    //     ExerciseVariant("Shoulder Press (Dumbbell)", "shoulder_press_dumbbell.jpg")
    // )
)

