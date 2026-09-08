package com.smartledger.aldaftar.data.serialization.pdf

import android.content.Context
import android.graphics.Bitmap
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.BusinessProfile

data class BusinessHeaderData(val displayedName:String,val displayedDesc:String,val phonesStr:String,val hasLogo:Boolean,val logoW:Float,val logoH:Float,val scaledLogo:Bitmap?,val rawBitmap:Bitmap?)
object BusinessProfileLoader {
 fun load(context:Context, profile: BusinessProfile):BusinessHeaderData {
  val displayedName=profile.name.trim().ifBlank{context.getString(R.string.app_name)}
  val displayedDesc=profile.description.trim().ifBlank{context.getString(R.string.pdf_default_desc)}
  val logoResult=PdfDrawingUtils.loadAndScaleLogo(context,profile.logoPath)
  val phones=profile.phones.filter(String::isNotBlank)
  val phonesStr=if(phones.isNotEmpty()) context.getString(R.string.pdf_phone_prefix)+" "+phones.joinToString(" - ") else context.getString(R.string.pdf_certified_identity)
  return BusinessHeaderData(displayedName,displayedDesc,phonesStr,logoResult.hasLogo,logoResult.width,logoResult.height,logoResult.bitmap,logoResult.rawBitmapToRecycle)
 }
}
