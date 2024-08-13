package net.rsprox.web.service

import net.rsprox.web.db.Submission

public interface FileUploader {

    public fun uploadFile(buf: ByteArray, submission: Submission): Boolean

}
