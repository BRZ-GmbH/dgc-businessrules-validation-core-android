package at.gv.brz.brvc.model.internal

import at.gv.brz.brvc.model.ConditionViolation

internal sealed class ConditionValidationResult {
    object Fulfilled: ConditionValidationResult()
    data class Violated(val violation: ConditionViolation): ConditionValidationResult()
    data class Failed(val condition: String): ConditionValidationResult()
}