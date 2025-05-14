package es.eriktorr
package report.api

import ReportGenerators.reportInfoGen

import io.circe.parser
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

final class ReportInfoSuite extends ScalaCheckSuite:
  property("decoding report info from json is reversible"):
    forAll(reportInfoGen()): reportInfo =>
      val obtained = for
        json <- parser.parse(rawJsonFrom(reportInfo))
        obtained <- json.as[ReportInfo]
      yield obtained
      val expected = Right(reportInfo).withLeft[io.circe.Error]
      assertEquals(obtained, expected, "decoded value preserves the original")

  private def rawJsonFrom(reportInfo: ReportInfo) =
    s"""{
       |  "sha1": "${reportInfo.sha1}",
       |  "cur": "${reportInfo.cur}",
       |  "company_name": "${reportInfo.companyName}",
       |  "major_industry": "${reportInfo.majorIndustry}",
       |  "mentions_recent_mergers_and_acquisitions": ${reportInfo.mentionsRecentMergersAndAcquisitions},
       |  "has_leadership_changes": ${reportInfo.hasLeadershipChanges},
       |  "has_layoffs": ${reportInfo.hasLayoffs},
       |  "has_executive_compensation": ${reportInfo.hasExecutiveCompensation},
       |  "has_rnd_investment_numbers": ${reportInfo.hasRndInvestmentNumbers},
       |  "has_new_product_launches": ${reportInfo.hasNewProductLaunches},
       |  "has_capital_expenditures": ${reportInfo.hasCapitalExpenditures},
       |  "has_financial_performance_indicators": ${reportInfo.hasFinancialPerformanceIndicators},
       |  "has_dividend_policy_changes": ${reportInfo.hasDividendPolicyChanges},
       |  "has_share_buyback_plans": ${reportInfo.hasShareBuybackPlans},
       |  "has_capital_structure_changes": ${reportInfo.hasCapitalStructureChanges},
       |  "mentions_new_risk_factors": ${reportInfo.mentionsNewRiskFactors},
       |  "has_guidance_updates": ${reportInfo.hasGuidanceUpdates},
       |  "has_regulatory_or_litigation_issues": ${reportInfo.hasRegulatoryOrLitigationIssues},
       |  "has_strategic_restructuring": ${reportInfo.hasStrategicRestructuring},
       |  "has_supply_chain_disruptions": ${reportInfo.hasSupplyChainDisruptions},
       |  "has_esg_initiatives": ${reportInfo.hasEsgInitiatives}
       |}""".stripMargin
