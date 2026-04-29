package com.example.kaizenfrontend.feature.workouts.domain.model

enum class SetType {
    NORMAL,
    WARMUP,
    DROP_SET,
    SUPER_SET,
    FAILURE,
    MYO_REP;
    
    fun getDisplayName(): String {
        return when (this) {
            NORMAL -> "Normal"
            WARMUP -> "Warmup"
            DROP_SET -> "Drop Set"
            SUPER_SET -> "Super Set"
            FAILURE -> "Failure"
            MYO_REP -> "Myo-rep"
        }
    }
}
