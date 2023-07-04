package at.gv.brz.brvc.model

/**
 * Enum for possible syntax errors in the BusinessRuleContainer
 */
sealed class BusinessRulesSyntaxError {
    /**
     * A condition is referenced that is not defined
     */
    data class UnavailableCondition(val conditionName: String): BusinessRulesSyntaxError()

    /**
     * A target group is referenced in a profile ruleset that is not defined for the rule
     */
    data class UnavailableTargetGroup(val targetGroup: String): BusinessRulesSyntaxError()

    /**
     * Rules for a profile that is not defined are specified
     */
    data class UnavailableProfile(val profile: String): BusinessRulesSyntaxError()

    /**
     * A reserved group name was used
     */
    data class ReservedTargetGroupName(val targetGroupName: String): BusinessRulesSyntaxError()

    /**
     * A profile that is not defined was linked in a ruleset via equal_to_profile
     */
    data class UnknownLinkedProfile(val profile: String): BusinessRulesSyntaxError()

    /**
     * The target group in which a equal_to_profile condition was specified does not exist in the target profile
     */
    data class UnknownTargetGroupInLinkedProfile(val profile: String, val targetGroup: String): BusinessRulesSyntaxError()

    /**
     * The target group and profile that is referenced also references another profile which is not allowed
     */
    data class UnallowedMultistepProfileChain(val profile: String, val targetGroup: String): BusinessRulesSyntaxError()
}