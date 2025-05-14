package es.eriktorr
package report.api

import io.circe.derivation.{Configuration, ConfiguredCodec}

object ReportInfo:
  given Configuration = Configuration.default.withDefaults.withSnakeCaseMemberNames

final case class ReportInfo(
    sha1: String,
    cur: String,
    companyName: String,
    majorIndustry: String,
    mentionsRecentMergersAndAcquisitions: Boolean = false,
    hasLeadershipChanges: Boolean = false,
    hasLayoffs: Boolean = false,
    hasExecutiveCompensation: Boolean = false,
    hasRndInvestmentNumbers: Boolean = false,
    hasNewProductLaunches: Boolean = false,
    hasCapitalExpenditures: Boolean = false,
    hasFinancialPerformanceIndicators: Boolean = false,
    hasDividendPolicyChanges: Boolean = false,
    hasShareBuybackPlans: Boolean = false,
    hasCapitalStructureChanges: Boolean = false,
    mentionsNewRiskFactors: Boolean = false,
    hasGuidanceUpdates: Boolean = false,
    hasRegulatoryOrLitigationIssues: Boolean = false,
    hasStrategicRestructuring: Boolean = false,
    hasSupplyChainDisruptions: Boolean = false,
    hasEsgInitiatives: Boolean = false,
) derives ConfiguredCodec
