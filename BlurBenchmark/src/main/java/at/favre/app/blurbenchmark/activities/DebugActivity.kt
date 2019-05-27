package at.favre.app.blurbenchmark.activities

import at.favre.app.blurbenchmark.BuildConfig
import at.favre.lib.hood.extended.PopHoodActivity
import at.favre.lib.hood.interfaces.Config
import at.favre.lib.hood.interfaces.Pages
import at.favre.lib.hood.util.PackageInfoAssembler
import at.favre.lib.hood.util.defaults.DefaultProperties

class DebugActivity : PopHoodActivity() {

    override fun getPageData(pages: Pages): Pages {
        pages.addNewPage().apply {
            add(DefaultProperties.createSectionSourceControlAndCI(BuildConfig.GIT_REV, BuildConfig.GIT_BRANCH, BuildConfig.GIT_DATE, BuildConfig.BUILD_NUMBER, null, BuildConfig.BUILD_DATE))
            add(DefaultProperties.createSectionBasicDeviceInfo())
            add(DefaultProperties.createDetailedDeviceInfo(this@DebugActivity))
            add(DefaultProperties.createSectionAppVersionInfoFromBuildConfig(BuildConfig::class.java))
            add(PackageInfoAssembler(PackageInfoAssembler.Type.PERMISSIONS,
                    PackageInfoAssembler.Type.SIGNATURE,
                    PackageInfoAssembler.Type.USES_FEATURE).createSection(this@DebugActivity))
        }
        return pages
    }

    override fun getConfig(): Config = Config.newBuilder().setShowZebra(false).build()
    
}