package com.lesofn.appforge.common.constant

import spock.lang.Specification

/**
 * Spock test for Constants
 * @author sofn
 */
class ConstantsTest extends Specification {

    def "test byte size constants have correct values"() {
        expect:
        Constants.KB == 1024
        Constants.MB == 1024 * 1024
        Constants.GB == 1024 * 1024 * 1024
    }

    def "test HTTP and HTTPS constants have correct values"() {
        expect:
        Constants.HTTP == "http://"
        Constants.HTTPS == "https://"
    }

    def "test RESOURCE_PREFIX constant has correct value"() {
        expect:
        Constants.RESOURCE_PREFIX == "profile"
    }

    def "test Token constants have correct values"() {
        expect:
        Constants.Token.PREFIX == "Bearer "
        Constants.Token.LOGIN_USER_KEY == "login_user_key"
    }

    def "test Captcha constants have correct values"() {
        expect:
        Constants.Captcha.MATH_TYPE == "math"
        Constants.Captcha.CHAR_TYPE == "char"
    }

    def "test UploadSubDir constants have correct values"() {
        expect:
        Constants.UploadSubDir.IMPORT_PATH == "import"
        Constants.UploadSubDir.AVATAR_PATH == "avatar"
        Constants.UploadSubDir.DOWNLOAD_PATH == "download"
        Constants.UploadSubDir.UPLOAD_PATH == "upload"
    }

    def "test byte size calculations are correct"() {
        expect:
        Constants.MB == Constants.KB * 1024
        Constants.GB == Constants.MB * 1024
        Constants.GB == Constants.KB * 1024 * 1024
    }

    def "test constants are suitable for their intended use cases"() {
        expect:
        // HTTP constants should include protocol separator
        Constants.HTTP.startsWith("http")
        Constants.HTTPS.startsWith("https")
        Constants.HTTP.endsWith("://")
        Constants.HTTPS.endsWith("://")
        
        // Token prefix should include space
        Constants.Token.PREFIX.endsWith(" ")
        
        // Upload paths should be lowercase and simple
        Constants.UploadSubDir.IMPORT_PATH == Constants.UploadSubDir.IMPORT_PATH.toLowerCase()
        Constants.UploadSubDir.AVATAR_PATH == Constants.UploadSubDir.AVATAR_PATH.toLowerCase()
        Constants.UploadSubDir.DOWNLOAD_PATH == Constants.UploadSubDir.DOWNLOAD_PATH.toLowerCase()
        Constants.UploadSubDir.UPLOAD_PATH == Constants.UploadSubDir.UPLOAD_PATH.toLowerCase()
    }
}