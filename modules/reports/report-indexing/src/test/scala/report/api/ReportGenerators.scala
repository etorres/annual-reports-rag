package es.eriktorr
package report.api

import common.spec.StringGenerators.{alphaNumericStringBetween, alphaNumericStringOf}

import org.scalacheck.Gen

object ReportGenerators:
  private val boolGen: Gen[Boolean] = Gen.oneOf(true, false)

  def reportInfoGen(
      sha1Gen: Gen[String] = alphaNumericStringOf(40),
      currencyGen: Gen[String] = alphaNumericStringOf(3),
      companyNameGen: Gen[String] = alphaNumericStringBetween(3, 7),
      majorIndustryGen: Gen[String] = alphaNumericStringBetween(3, 7),
  ): Gen[ReportInfo] =
    for
      sha1 <- sha1Gen
      currency <- currencyGen
      companyName <- companyNameGen
      majorIndustry <- majorIndustryGen
      mentionsRecentMergersAndAcquisitions <- boolGen
      hasLeadershipChanges <- boolGen
      hasLayoffs <- boolGen
      hasExecutiveCompensation <- boolGen
      hasRndInvestmentNumbers <- boolGen
      hasNewProductLaunches <- boolGen
      hasCapitalExpenditures <- boolGen
      hasFinancialPerformanceIndicators <- boolGen
      hasDividendPolicyChanges <- boolGen
      hasShareBuybackPlans <- boolGen
      hasCapitalStructureChanges <- boolGen
      mentionsNewRiskFactors <- boolGen
      hasGuidanceUpdates <- boolGen
      hasRegulatoryOrLitigationIssues <- boolGen
      hasStrategicRestructuring <- boolGen
      hasSupplyChainDisruptions <- boolGen
      hasEsgInitiatives <- boolGen
    yield ReportInfo(
      sha1,
      currency,
      companyName,
      majorIndustry,
      mentionsRecentMergersAndAcquisitions,
      hasLeadershipChanges,
      hasLayoffs,
      hasExecutiveCompensation,
      hasRndInvestmentNumbers,
      hasNewProductLaunches,
      hasCapitalExpenditures,
      hasFinancialPerformanceIndicators,
      hasDividendPolicyChanges,
      hasShareBuybackPlans,
      hasCapitalStructureChanges,
      mentionsNewRiskFactors,
      hasGuidanceUpdates,
      hasRegulatoryOrLitigationIssues,
      hasStrategicRestructuring,
      hasSupplyChainDisruptions,
      hasEsgInitiatives,
    )
