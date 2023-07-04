package at.gv.brz.brvc

import at.gv.brz.brvc.model.data.BusinessRuleContainer
import com.squareup.moshi.Moshi
import java.time.ZonedDateTime

open class BusinessRulesTest {

    val externalConditionEvaluator = ExternalConditionTestEvaluator()

    fun getBusinessRules(path: String): BusinessRuleContainer? {
        val ruleData = this.javaClass.classLoader.getResource(path + ".json")?.readText() ?: return null
        return BusinessRuleContainer.fromData(ruleData)
    }

    fun getValidationCore(rulesPath: String, externalConditionEvaluationStrategy: ExternalConditionEvaluationStrategy = ExternalConditionEvaluationStrategy.FAIL_CONDITION, validationClock: ZonedDateTime = ZonedDateTime.now()): BusinessRulesValidator? {
        val businessRules = getBusinessRules(path = rulesPath) ?: return null

        val valueSetsData = this.javaClass.classLoader.getResource("valuesets.json")?.readText() ?: return null
        val valueSets = Moshi.Builder().build().adapter(Map::class.java).fromJson(valueSetsData) as Map<String, List<String>>

        return BusinessRulesValidator(
            businessRules = businessRules,
            valueSets = valueSets,
            validationClock = validationClock,
            externalConditionEvaluator = externalConditionEvaluator,
            externalConditionEvaluationStrategy = externalConditionEvaluationStrategy
        )
    }
}