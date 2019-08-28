/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.vision;

import com.google.api.gax.longrunning.OperationFuture;

import com.google.cloud.vision.v1.BatchOperationMetadata;
import com.google.cloud.vision.v1.LocationName;
import com.google.cloud.vision.v1.Product;
import com.google.cloud.vision.v1.ProductName;
import com.google.cloud.vision.v1.ProductSearchClient;
import com.google.cloud.vision.v1.ProductSetPurgeConfig;
import com.google.cloud.vision.v1.PurgeProductsRequest;

import com.google.protobuf.Empty;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * This application demonstrates how to perform basic operations with Products in a Product Set.
 *
 * <p>For more information, see the tutorial page at
 * https://cloud.google.com/vision/product-search/docs/
 */
public class ProductInProductSetManagement {

  // [START vision_product_search_add_product_to_product_set]
  /**
   * Add a product to a product set.
   *
   * @param projectId - Id of the project.
   * @param computeRegion - Region name.
   * @param productId - Id of the product.
   * @param productSetId - Id of the product set.
   * @throws IOException - on I/O errors.
   */
  public static void addProductToProductSet(
          String projectId, String computeRegion, String productId, String productSetId)
          throws IOException {
    try (ProductSearchClient client = ProductSearchClient.create()) {

      // Get the full path of the product set.
      String formattedName =
              ProductSearchClient.formatProductSetName(projectId, computeRegion, productSetId);

      // Get the full path of the product.
      String productPath = ProductName.of(projectId, computeRegion, productId).toString();

      // Add the product to the product set.
      client.addProductToProductSet(formattedName, productPath);

      System.out.println(String.format("Product added to product set."));
    }
  }
  // [END vision_product_search_add_product_to_product_set]

  // [START vision_product_search_list_products_in_product_set]
  /**
   * List all products in a product set.
   *
   * @param projectId - Id of the project.
   * @param computeRegion - Region name.
   * @param productSetId - Id of the product set.
   * @throws IOException - on I/O errors.
   */
  public static void listProductsInProductSet(
          String projectId, String computeRegion, String productSetId) throws IOException {
    try (ProductSearchClient client = ProductSearchClient.create()) {

      // Get the full path of the product set.
      String formattedName =
              ProductSearchClient.formatProductSetName(projectId, computeRegion, productSetId);
      // List all the products available in the product set.
      for (Product product : client.listProductsInProductSet(formattedName).iterateAll()) {
        // Display the product information
        System.out.println(String.format("Product name: %s", product.getName()));
        System.out.println(
                String.format(
                        "Product id: %s",
                        product.getName().substring(product.getName().lastIndexOf('/') + 1)));
        System.out.println(String.format("Product display name: %s", product.getDisplayName()));
        System.out.println(String.format("Product description: %s", product.getDescription()));
        System.out.println(String.format("Product category: %s", product.getProductCategory()));
        System.out.println("Product labels: ");
        for (Product.KeyValue element : product.getProductLabelsList()) {
          System.out.println(String.format("%s: %s", element.getKey(), element.getValue()));
        }
      }
    }
  }
  // [END vision_product_search_list_products_in_product_set]

  // [START vision_product_search_remove_product_from_product_set]
  /**
   * Remove a product from a product set.
   *
   * @param projectId - Id of the project.
   * @param computeRegion - Region name.
   * @param productId - Id of the product.
   * @param productSetId - Id of the product set.
   * @throws IOException - on I/O errors.
   */
  public static void removeProductFromProductSet(
          String projectId, String computeRegion, String productId, String productSetId)
          throws IOException {
    try (ProductSearchClient client = ProductSearchClient.create()) {

      // Get the full path of the product set.
      String formattedParent =
              ProductSearchClient.formatProductSetName(projectId, computeRegion, productSetId);

      // Get the full path of the product.
      String formattedName =
              ProductSearchClient.formatProductName(projectId, computeRegion, productId);

      // Remove the product from the product set.
      client.removeProductFromProductSet(formattedParent, formattedName);

      System.out.println(String.format("Product removed from product set."));
    }
  }
  // [END vision_product_search_remove_product_from_product_set]

  // [START vision_product_search_purge_products_in_product_set]
  /**
   * Delete all products in a product set.
   *
   * @param projectId - Id of the project.
   * @param location - Region name.
   * @param productSetId - Id of the product set.
   * @param force - Perform the purge only when force is set to True.
   * @throws Exception - any error.
   */
  public static void purgeProductsInProductSet(
          String projectId, String location, String productSetId, boolean force)
          throws Exception {
    try (ProductSearchClient client = ProductSearchClient.create()) {

      String parent = LocationName.format(projectId, location);
      ProductSetPurgeConfig productSetPurgeConfig = ProductSetPurgeConfig
              .newBuilder()
              .setProductSetId(productSetId)
              .build();

      PurgeProductsRequest req = PurgeProductsRequest
              .newBuilder()
              .setParent(parent)
              .setProductSetPurgeConfig(productSetPurgeConfig)
              // The operation is irreversible and removes multiple products.
              // The user is required to pass in force=True to actually perform the
              // purge.
              // If force is not set to True, the service raises an exception.
              .setForce(force)
              .build();

      OperationFuture<Empty, BatchOperationMetadata> response = client.purgeProductsAsync(req);

      while (!response.isDone()) {
        Thread.sleep(1000);
      }
      //TODO: replace with this --> as soon as : response.get(60, TimeUnit.SECONDS);

      System.out.println("Products removed from product set.");
    }

  }
  // [END vision_product_search_purge_products_in_product_set]

  public static void main(String[] args) throws Exception {
    ProductInProductSetManagement productInProductSetManagement =
            new ProductInProductSetManagement();
    productInProductSetManagement.argsHelper(args, System.out);
  }

  public static void argsHelper(String[] args, PrintStream out) throws Exception {
    ArgumentParser parser = ArgumentParsers.newFor("").build();
    Subparsers subparsers = parser.addSubparsers().dest("command");

    Subparser addProductParser = subparsers.addParser("add_product_to_product_set");
    addProductParser.addArgument("productSetId");
    addProductParser.addArgument("productId");

    Subparser listProductInProductSetParser = subparsers.addParser("list_products_in_product_set");
    listProductInProductSetParser.addArgument("productSetId");

    Subparser removeProductFromProductSetParser =
            subparsers.addParser("remove_product_from_product_set");
    removeProductFromProductSetParser.addArgument("productId");
    removeProductFromProductSetParser.addArgument("productSetId");

    Subparser purgeProductsInProductSetParser =
            subparsers.addParser("purge_products_in_product_set");
    purgeProductsInProductSetParser.addArgument("productSetId");
    purgeProductsInProductSetParser.addArgument("force");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("add_product_to_product_set")) {
        addProductToProductSet(
                projectId, computeRegion, ns.getString("productId"), ns.getString("productSetId"));
      }
      if (ns.get("command").equals("list_products_in_product_set")) {
        listProductsInProductSet(projectId, computeRegion, ns.getString("productSetId"));
      }
      if (ns.get("command").equals("remove_product_from_product_set")) {
        removeProductFromProductSet(
                projectId, computeRegion, ns.getString("productId"), ns.getString("productSetId"));
      }
      System.out.println(ns.getAttrs());
      if (ns.get("command").equals("purge_products_in_product_set")) {
        purgeProductsInProductSet(
                projectId, computeRegion, ns.getString("productSetId"),
                Boolean.parseBoolean(ns.getString("force")));
      }

    } catch (ArgumentParserException e) {
      parser.handleError(e);
    }
  }
}
