package es.eriktorr
package report.api

import common.spec.TestFilters.envVarsName

import munit.CatsEffectSuite

import scala.util.Properties

final class ReportInfoLoaderSuite extends CatsEffectSuite:
  test("should read report info from file (mandatory fields)"):
    testWith(
      ReportInfo(
        sha1 = "d81bbc64a4160b9946fea7a895f80e6201f52f27",
        cur = "USD",
        companyName = "Air Products and Chemicals, Inc.",
        majorIndustry = "Energy and Utilities",
      ),
      simple = true,
    )

  test("should read report info from file (all fields)"):
    testWith(
      ReportInfo(
        sha1 = "4b525836a5d7cb75489f6d93a3b1cf2b8f039bf2",
        cur = "USD",
        companyName = "TD SYNNEX Corporation",
        majorIndustry = "Technology",
        mentionsRecentMergersAndAcquisitions = true,
        hasLeadershipChanges = true,
        hasExecutiveCompensation = true,
        hasRndInvestmentNumbers = true,
        hasNewProductLaunches = true,
        hasCapitalExpenditures = true,
        hasFinancialPerformanceIndicators = true,
        hasDividendPolicyChanges = true,
        hasShareBuybackPlans = true,
        mentionsNewRiskFactors = true,
        hasGuidanceUpdates = true,
        hasRegulatoryOrLitigationIssues = true,
        hasStrategicRestructuring = true,
        hasSupplyChainDisruptions = true,
        hasEsgInitiatives = true,
      ),
      simple = false,
    )

  private def testWith(reportInfo: ReportInfo, simple: Boolean) =
    ReportInfoLoader(pathToReportsInfo)
      .findReportInfoBy(reportInfo.sha1, simple)
      .value
      .assertEquals(Some(reportInfo))

  private def pathToReportsInfo =
    val wd = os.pwd
    val reportsPath =
      if Properties.envOrNone(envVarsName).nonEmpty
      then wd / os.up
      else wd / "modules" / "reports"
    val samplesPath = os.rel / "sample-reports" / "src" / "main" / "resources" / "samples"
    val subsetPath = samplesPath / "subset.json"
    reportsPath / subsetPath
