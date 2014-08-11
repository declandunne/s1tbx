/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.nest.dataio.sentinel1;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;
import org.esa.snap.util.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Test Product Reader.
 *
 * @author lveci
 */
public class TestSentinel1ProductReader {

    private Sentinel1ProductReaderPlugIn readerPlugin;
    private ProductReader reader;

    final String s1ZipFilePath = "J:\\Data\\zips\\S1A_S1_SLC__1SDV_20140607T172812_20140607T172836_000947_000EBD_4DB2.zip";
    final String s1FolderFilePath = "P:\\s1tbx\\s1tbx\\Data\\First Images\\S1A_S1_SLC__1SDV_20140607T172812_20140607T172836_000947_000EBD_4DB2.SAFE";

    @Before
    public void setUp() throws Exception {
        TestUtils.initTestEnvironment();
        readerPlugin = new Sentinel1ProductReaderPlugIn();
        reader = readerPlugin.createReaderInstance();
    }

    /**
     * Open all files in a folder recursively
     *
     * @throws Exception anything
     */
    @Test
    public void testOpenAll() throws Exception {
        final File folder = new File(TestUtils.rootPathSentinel1);
        if (!folder.exists()) {
            TestUtils.skipTest(this);
            return;
        }

        if (TestUtils.canTestReadersOnAllProducts)
            TestUtils.recurseReadFolder(folder, readerPlugin, reader, null, null);
    }

    @Test
    public void testOpeningFolder() throws Exception {
        final File inputFile = new File(s1FolderFilePath, "manifest.safe");
        if(!inputFile.exists())
            TestUtils.skipTest(this);

        final DecodeQualification canRead = readerPlugin.getDecodeQualification(inputFile);
        Assert.assertTrue(canRead == DecodeQualification.INTENDED);

        final Product product = reader.readProductNodes(inputFile, null);
        Assert.assertTrue(product != null);
    }

    @Test
    public void testOpeningZip() throws Exception {
        final File inputFile = new File(s1ZipFilePath);
        if(!inputFile.exists())
            TestUtils.skipTest(this);

        final DecodeQualification canRead = readerPlugin.getDecodeQualification(inputFile);
        Assert.assertTrue(canRead == DecodeQualification.INTENDED);

        final Product product = reader.readProductNodes(inputFile, null);
        Assert.assertTrue(product != null);
    }

 /*   @Test
    public void testOpeningInputStream() throws Exception {
        final File inputFile = new File(s1ZipFilePath);
        if(!inputFile.exists())
            TestUtils.skipTest(this);

        final InputStream inputStream = new FileInputStream(s1ZipFilePath);

        final DecodeQualification canRead = readerPlugin.getDecodeQualification(inputStream);
        Assert.assertTrue(canRead == DecodeQualification.INTENDED);

        final Product product = reader.readProductNodes(inputStream, null);
        Assert.assertTrue(product != null);
    }*/
}