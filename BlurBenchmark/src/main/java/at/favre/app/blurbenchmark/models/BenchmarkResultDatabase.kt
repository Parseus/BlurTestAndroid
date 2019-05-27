package at.favre.app.blurbenchmark.models

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

/**
 * This is the main logic on retrieving specific data from the benchmark database
 *
 * @author pfavre
 */
@Suppress("RemoveEmptyPrimaryConstructor")
class BenchmarkResultDatabase() {
    var entryList: MutableList<BenchmarkEntry> = ArrayList()

    val allImageSizes: TreeSet<ImageSize>
        @JsonIgnore
        get() {
            val set = TreeSet<ImageSize>()
            for (benchmarkEntry in entryList) {
                set.add(benchmarkEntry.asImageSize)
            }
            return set
        }

    val allBlurRadii: Set<Int>
        @JsonIgnore
        get() {
            val list = TreeSet<Int>()
            for (benchmarkEntry in entryList) {
                list.add(benchmarkEntry.radius)
            }
            return list
        }

    @JsonIgnore
    fun getByName(name: String): BenchmarkEntry? {
        for (benchmarkEntry in entryList) {
            if (benchmarkEntry.name == name) {
                return benchmarkEntry
            }
        }
        return null
    }

    @JsonIgnore
    fun getAllByCategory(category: String): List<BenchmarkEntry> {
        val list = ArrayList<BenchmarkEntry>()
        for (benchmarkEntry in entryList) {
            if (benchmarkEntry.category == category) {
                list.add(benchmarkEntry)
            }
        }
        return list
    }

    @JsonIgnore
    fun getAllByBlurRadius(radius: Int): List<BenchmarkEntry> {
        val list = ArrayList<BenchmarkEntry>()
        for (benchmarkEntry in entryList) {
            if (benchmarkEntry.radius == radius) {
                list.add(benchmarkEntry)
            }
        }
        return list
    }

    @JsonIgnore
    fun getByImageSizeAndRadiusAndAlgorithm(imageSize: String, radius: Int, algorithm: EBlurAlgorithm): BenchmarkEntry? {
        for (benchmarkEntry in entryList) {
            if (benchmarkEntry.imageSizeString == imageSize && benchmarkEntry.radius == radius && benchmarkEntry.wrapper.isNotEmpty() && benchmarkEntry.wrapper[0].statInfo.algorithm == algorithm) {
                return benchmarkEntry
            }
        }
        return null
    }

    @JsonIgnore
    fun getByCategoryAndAlgorithm(category: String, algorithm: EBlurAlgorithm): BenchmarkEntry? {
        for (benchmarkEntry in entryList) {
            if (benchmarkEntry.category == category) {
                if (benchmarkEntry.wrapper.isNotEmpty() && benchmarkEntry.wrapper[0].statInfo.algorithm == algorithm) {
                    return benchmarkEntry
                }
            }
        }
        return null
    }

    class BenchmarkEntry : Comparable<BenchmarkEntry> {
        var name: String? = null
        var category: String? = null
        var radius: Int = 0
        var height: Int = 0
        var width: Int = 0
        var wrapper: MutableList<BenchmarkWrapper> = ArrayList()

        val categoryObj: Category
            @JsonIgnore
            get() = Category(asImageSize, radius, category!!)

        private val resolution: Int?
            @JsonIgnore
            get() = height * width

        val imageSizeString: String
            @JsonIgnore
            get() = height.toString() + "x" + width

        val asImageSize: ImageSize
            @JsonIgnore
            get() = ImageSize(height, width, imageSizeString)

        @Suppress("unused")
        constructor()

        constructor(name: String, category: String, radius: Int, height: Int, width: Int, wrapper: MutableList<BenchmarkWrapper>) {
            this.name = name
            this.category = category
            this.wrapper = wrapper
            this.radius = radius
            this.height = height
            this.width = width
        }

        constructor(benchmarkWrapper: BenchmarkWrapper) : this(benchmarkWrapper.statInfo.keyString,
                benchmarkWrapper.statInfo.categoryString, benchmarkWrapper.statInfo.blurRadius,
                benchmarkWrapper.statInfo.bitmapHeight, benchmarkWrapper.statInfo.bitmapWidth, ArrayList<BenchmarkWrapper>())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false

            val that = other as BenchmarkEntry?

            return if (name != null) name == that!!.name else that!!.name == null
        }

        override fun hashCode(): Int {
            return if (name != null) name!!.hashCode() else 0
        }

        override fun compareTo(other: BenchmarkEntry): Int {
            return resolution!!.compareTo(other.resolution!!)
        }
    }

    data class Category(private val imageSize: ImageSize, val radius: Int, val category: String) : Comparable<Category> {

        override fun compareTo(other: Category): Int {
            val resultResolution = imageSize.resolution.compareTo(other.imageSize.resolution)

            return if (resultResolution == 0) {
                radius.compareTo(other.radius)
            } else {
                resultResolution
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false

            val category = other as Category?

            return if (radius != category!!.radius) false else imageSize == category.imageSize
        }

        override fun hashCode(): Int {
            var result = imageSize.hashCode()
            result = 31 * result + radius
            return result
        }
    }

    class ImageSize(val height: Int, val width: Int, val imageSizeString: String) : Comparable<ImageSize> {

        val resolution: Int
            get() = height * width

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false

            val imageSize = other as ImageSize?

            return if (height != imageSize!!.height) false else width == imageSize.width
        }

        override fun hashCode(): Int {
            var result = height
            result = 31 * result + width
            return result
        }

        override fun compareTo(other: ImageSize): Int {
            return resolution.compareTo(other.resolution)
        }
    }

    companion object {

        @JsonIgnore
        fun getRecentWrapper(entry: BenchmarkEntry?): BenchmarkWrapper? {
            return if (entry != null && entry.wrapper.isNotEmpty()) {
                entry.wrapper.sorted()[0]
            } else {
                null
            }
        }
    }
}
