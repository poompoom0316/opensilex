//******************************************************************************
//                                       WasGeneratedByDTO.java
//
// Author(s): Morgane Vidal <morgane.vidal@inra.fr>
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: 17 janv. 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  17 janv. 2018
// Subject:Represents the submitted JSON for the was generated by data 
// (used in provenance)
//******************************************************************************
package opensilex.service.resources.dto;

import io.swagger.annotations.ApiModelProperty;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.resources.dto.manager.AbstractVerifiedClass;
import opensilex.service.resources.validation.interfaces.URL;
import opensilex.service.model.WasGeneratedBy;

/**
 * Represents the submitted JSON for the was generated by data 
 * (used in provenance)
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class WasGeneratedByDTO extends AbstractVerifiedClass {
    
    //a document uri corresponding to a script used to generate a dataset for 
    //example. 
    //e.g. http://www.phenome-fppn.fr/phis_field/documents/documente597f57ba71d421a86277d830f4b9885
    private String wasGeneratedByDocument;
    //a description of how the dataset was generated.
    //e.g Phenoscript v1.3
    private String wasGeneratedByDescription;

    @Override
    public WasGeneratedBy createObjectFromDTO() {
        WasGeneratedBy wasGeneratedBy = new WasGeneratedBy();
        wasGeneratedBy.setWasGeneratedBy(wasGeneratedByDocument);
        wasGeneratedBy.setWasGeneratedByDescription(wasGeneratedByDescription);
        return wasGeneratedBy;
    }

    @URL
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_WAS_GENERATED_BY_DOCUMENT)
    public String getWasGeneratedByDocument() {
        return wasGeneratedByDocument;
    }

    public void setWasGeneratedByDocument(String wasGeneratedByDocument) {
        this.wasGeneratedByDocument = wasGeneratedByDocument;
    }

    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_WAS_GENERATED_BY_DESCRIPTION)
    public String getWasGeneratedByDescription() {
        return wasGeneratedByDescription;
    }

    public void setWasGeneratedByDescription(String wasGeneratedByDescription) {
        this.wasGeneratedByDescription = wasGeneratedByDescription;
    }
}
