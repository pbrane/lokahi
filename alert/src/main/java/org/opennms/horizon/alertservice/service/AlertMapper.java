package org.opennms.horizon.alertservice.service;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.AlertAssociation;
import org.opennms.horizon.alertservice.model.AlertAssociationDTO;
import org.opennms.horizon.alertservice.model.AlertDTO;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    AlertMapper INSTANCE = Mappers.getMapper( AlertMapper.class );

    AlertDTO alertToAlertDTO(Alert alert);
    Alert alertDTOToAlert(AlertDTO alertDTO);

    AlertAssociationDTO alertAssociationToAlertAssociationDTO(AlertAssociation alertAssociation);
    AlertAssociation alertAssociationDTOToAlertAssociation(AlertAssociationDTO alertAssociationDTO);

}
