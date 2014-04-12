/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.assemble;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Implements PutTarget such that files get uploaded to S3
 */
public class S3PutObjectTarget implements PutTarget {

   private final String bucket;
   private final AmazonS3 s3;
   private final HashMap<String, String> etags;

   /**
    * @param bucket the bucket where we intend to upload the files
    * @param s3     the S3 client
    */
   public S3PutObjectTarget(final String bucket, final AmazonS3 s3) {
      this.bucket = bucket;
      this.s3 = s3;
      this.etags = new HashMap<>();
      String marker = "";
      boolean again = true;
      while (again) {
         again = false;
         ListObjectsRequest request = new ListObjectsRequest(bucket, "", marker, null, 1000);
         for (S3ObjectSummary obj : s3.listObjects(request).getObjectSummaries()) {
            marker = obj.getKey();
            if (marker.startsWith("charts/")) {
               s3.deleteObject(bucket, marker);
            }
            System.out.println(marker + "->" + obj.getETag());
            etags.put(obj.getKey(), obj.getETag());
            again = true;
         }
      }
   }

   @Override
   public void upload(String key, String md5, String contentType, InputStream body, long contentLength) throws Exception {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(contentLength);
      metadata.setContentType(contentType);
      if (md5.equalsIgnoreCase(etags.get(key))) {
         System.out.println("skipping:" + key);
         return;
      }
      System.out.println("uploading:" + key);
      s3.putObject(new PutObjectRequest(bucket, key, body, metadata).withCannedAcl(CannedAccessControlList.PublicRead));
   }
}
