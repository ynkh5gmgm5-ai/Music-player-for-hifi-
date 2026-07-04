package android.net

import android.os.Parcel

class FakeUri(private val value: String = "content://test/track") : Uri() {
    override fun isHierarchical(): Boolean = true
    override fun isRelative(): Boolean = false
    override fun getScheme(): String = value.substringBefore("://", "")
    override fun getSchemeSpecificPart(): String = value.substringAfter("://", "")
    override fun getEncodedSchemeSpecificPart(): String = schemeSpecificPart
    override fun getAuthority(): String = schemeSpecificPart.substringBefore("/", "")
    override fun getEncodedAuthority(): String = authority
    override fun getUserInfo(): String? = null
    override fun getEncodedUserInfo(): String? = null
    override fun getHost(): String = authority
    override fun getPort(): Int = -1
    override fun getPath(): String = "/" + schemeSpecificPart.substringAfter("/", "")
    override fun getEncodedPath(): String = path
    override fun getQuery(): String? = null
    override fun getEncodedQuery(): String? = null
    override fun getFragment(): String? = null
    override fun getEncodedFragment(): String? = null
    override fun getPathSegments(): List<String> = path.trim('/').takeIf { it.isNotBlank() }?.split("/") ?: emptyList()
    override fun getLastPathSegment(): String? = pathSegments.lastOrNull()
    override fun toString(): String = value
    override fun buildUpon(): Builder = Builder().scheme(scheme).authority(authority).path(path)
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = Unit
}
