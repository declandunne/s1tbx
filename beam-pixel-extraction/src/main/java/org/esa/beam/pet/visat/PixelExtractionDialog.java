/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.beam.pet.visat;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorSpiRegistry;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.pet.PetOp;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

class PixelExtractionDialog extends ModalDialog {

    private final Map<String, Object> parameterMap;
    private final AppContext appContext;
    private final PixelExtractionIOForm ioForm;
    private PixelExtractionParametersForm parametersForm;

    PixelExtractionDialog(AppContext appContext, String title) {
        super(appContext.getApplicationWindow(), title, ID_OK | ID_CLOSE | ID_HELP, "pixelExtraction");

        this.appContext = appContext;

        AbstractButton button = getButton(ID_OK);
        button.setText("Extract");
        button.setMnemonic('E');

        parameterMap = new HashMap<String, Object>();
        final PropertyContainer propertyContainer = createParameterMap(parameterMap);

        ioForm = new PixelExtractionIOForm(appContext, propertyContainer);
        parametersForm = new PixelExtractionParametersForm(appContext, propertyContainer);
        JTabbedPane tabbedPanel = new JTabbedPane();
        tabbedPanel.addTab("Input/Output", ioForm.getPanel());
        tabbedPanel.addTab("Parameters", parametersForm.getPanel());

        setContent(tabbedPanel);
    }

    @Override
    protected void onOK() {
        parameterMap.put("coordinates", parametersForm.getCoordinates());
        parameterMap.put("expression", parametersForm.getExpression());
        parameterMap.put("exportExpressionResult", parametersForm.isExportExpressionResultSelected());
        ProgressMonitorSwingWorker worker = new MyProgressMonitorSwingWorker(getParent(), "Creating output file(s)...");
        AbstractButton runButton = getButton(ID_OK);
        runButton.setEnabled(false);
        worker.execute();
    }

    @Override
    public void close() {
        super.close();
        ioForm.clear();
    }

    @Override
    public int show() {
        ioForm.setSelectedProduct();
        return super.show();
    }

    private PropertyContainer createParameterMap(Map<String, Object> map) {
        ParameterDescriptorFactory parameterDescriptorFactory = new ParameterDescriptorFactory();
        final PropertyContainer container = PropertyContainer.createMapBacked(map, PetOp.class,
                                                                              parameterDescriptorFactory);
        try {
            container.setDefaultValues();
        } catch (ValidationException e) {
            e.printStackTrace();
            showErrorDialog(e.getMessage());
        }
        return container;
    }

    private class MyProgressMonitorSwingWorker extends ProgressMonitorSwingWorker<Void, Void> {

        protected MyProgressMonitorSwingWorker(Component parentComponent, String title) {
            super(parentComponent, title);
        }

        @Override
        protected Void doInBackground(ProgressMonitor pm) throws Exception {
            pm.beginTask("Computing pixel values...", 1);
            try {
                GPF.createProduct("Pet", parameterMap, ioForm.getSourceProducts());
                pm.worked(1);
            } finally {
                pm.done();
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                Object outputDir = parameterMap.get("outputDir");
                String message;
                if (outputDir != null) {
                    message = String.format(
                            "The pixel extraction tool has run successfully and written the result file(s) to %s.",
                            outputDir.toString());
                } else {
                    message = "The pixel extraction tool has run successfully and written the result file to the clipboard.";
                }

                JOptionPane.showMessageDialog(getJDialog(), message);
            } catch (InterruptedException ignore) {
            } catch (ExecutionException e) {
                appContext.handleError(e.getMessage(), e);
            } finally {
                AbstractButton runButton = getButton(ID_OK);
                runButton.setEnabled(true);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        final DefaultAppContext context = new DefaultAppContext("dev0");
        final OperatorSpiRegistry registry = GPF.getDefaultInstance().getOperatorSpiRegistry();
        registry.addOperatorSpi(new PetOp.Spi());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final PixelExtractionDialog dialog = new PixelExtractionDialog(context, "Pixel Extraction") {
                    @Override
                    protected void onClose() {
                        System.exit(0);
                    }
                };
                dialog.getJDialog().setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.show();
            }
        });
    }

}
