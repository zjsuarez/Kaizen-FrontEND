package com.example.kaizenfrontend.feature.workouts.domain.model

enum class SetType {
    NORMAL,
    WARMUP,
    DROP_SET,
    FAILURE,
    MYO_REP;
    
    fun getDisplayName(): String {
        return when (this) {
            NORMAL -> "Normal"
            WARMUP -> "Warmup"
            DROP_SET -> "Drop Set"
            FAILURE -> "Failure"
            MYO_REP -> "Myo-rep"
        }
    }
}
