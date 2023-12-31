package com.ontimize.hr.model.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;

import com.ontimize.jee.common.services.user.UserInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ontimize.hr.api.core.service.ITravelsService;
import com.ontimize.hr.model.core.dao.*;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.springframework.transaction.annotation.Transactional;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import org.springframework.security.access.annotation.Secured;

@Service("TravelsService")
@Lazy
public class TravelsService implements ITravelsService {
    @Autowired private TravelsDao travelsDao;
    @Autowired private PlatesService platesService;
    @Autowired private TrailerPlatesService trailerPlatesService;
    @Autowired private DeliveryNotesService deliverynotesService;
    @Autowired private DevicesService devicesService;
    @Autowired private DefaultOntimizeDaoHelper daoHelper;


    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelFullQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.travelsDao, keyMap, attrList);
    }
    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelGetBalanceQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.travelsDao, keyMap, attrList, TravelsDao.QUERY_GET_BALANCE);
    }
    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelGetStockQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.travelsDao, keyMap, attrList, TravelsDao.QUERY_GET_STOCK);
    }
    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelGetTrucksQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.travelsDao, keyMap, attrList, TravelsDao.QUERY_GET_TRUCKS);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult restrictedTravelsByUserQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException{
        /*
        0. Hacer el servicio
        1. Obtener las matrículas con el Authorization.
        2. Hacer el basicExpression con OR para cada matrícula.
        3. Reemplazar el KeyMap con el BasicExpression.
        4. Hacer la consulta y retornarla.
        */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResponseEntity<String> re = null;
        keyMap.clear();
        List<String> attr = new ArrayList<>();
        keyMap.put("user_", ((UserInformation)authentication.getPrincipal()).getUsername());
        attr.add(PlatesDao.ATTR_PLATE_NUMBER);

        EntityResult et = platesService.userPlatesQuery(keyMap, attr);
        int num_plates = et.calculateRecordNumber();
        ArrayList<String> plates = new ArrayList<String>();
        BasicExpression exp = null;
        Map<String, Object> keys = new HashMap<String, Object>();
        if(num_plates > 0){
            for(int i = 0 ; i< num_plates ; i ++){
                plates.add((String) et.getRecordValues(i).get(PlatesDao.ATTR_PLATE_NUMBER));
            }


            if(plates.size() > 1){
                BasicField field = new BasicField(PlatesDao.ATTR_PLATE_NUMBER);

                for(int i = 0 ; i < num_plates; i ++){
                    if(i == 0){
                        exp = new BasicExpression(field, BasicOperator.EQUAL_OP, plates.get(i));
                    }else {
                        BasicExpression bexp1 = new BasicExpression(field, BasicOperator.EQUAL_OP, plates.get(i));
                        exp = new BasicExpression(exp, BasicOperator.OR_OP, bexp1);
                    }
                }
            }else{
                keys.put(PlatesDao.ATTR_PLATE_NUMBER,plates.get(0));
            }
        }

        keys.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, exp);
        return this.daoHelper.query(this.travelsDao, keys, attrList, TravelsDao.QUERY_GET_TRAVELS_TRUCK);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelQuery(Map<String, Object> keyMap, List<String> attrList)
            throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.travelsDao, keyMap, attrList);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
            throws OntimizeJEERuntimeException {
        Map <String, Object> nonTravelData = obtainNonRelatedData(attrMap,
                TravelsDao.ATTR_ID_DEV_OUT,
                TravelsDao.ATTR_ID_PLATE,
                TravelsDao.ATTR_ID_TRAILER_PLATE,
                TravelsDao.ATTR_ID_DELIVERY_NOTE
        );
        //Introducimos los campos modificados con los ids en el attrMap.
        attrMap.putAll(this.updateNonRelatedData(nonTravelData));
        return this.daoHelper.update(this.travelsDao, attrMap, keyMap);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    public EntityResult travelDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
        return this.daoHelper.delete(this.travelsDao, keyMap);
    }

    @Override
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    public EntityResult travelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
        Map <String, Object> nonTravelData = obtainNonRelatedData(attrMap,
                TravelsDao.ATTR_ID_DEV_IN,
                TravelsDao.ATTR_ID_PLATE,
                TravelsDao.ATTR_ID_TRAILER_PLATE,
                TravelsDao.ATTR_ID_DELIVERY_NOTE
        );
        //Introducimos los campos modificados con los ids en el attrMap.
        attrMap.putAll(this.insertNonRelatedData(nonTravelData));
        return this.daoHelper.insert(this.travelsDao, attrMap);
    }

    private Map<String, Object> obtainNonRelatedData(Map<String, Object> attrMap, String... attrToExclude){
        HashMap <String,Object> nonRelateData = new HashMap<>();
        for(String attr : attrToExclude){
            if(attrMap.containsKey(attr)){
                nonRelateData.put(attr, attrMap.remove(attr));
            }
        }
        return nonRelateData;
    }


    private Map<String,Object> insertNonRelatedData(Map<String, Object> inputMap){
        Map<String, Object> attrMap = new HashMap<>(inputMap);

        for(Map.Entry<String, Object>entry : attrMap.entrySet()){
            Map<String, Object> data = new HashMap<>();
            List<String> attr = new ArrayList<>();
            EntityResult toret;
            EntityResult query;

            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_DEV_IN)) {
                data.put(DevicesDao.ATTR_DEVICE_NAME, entry.getValue());
                attr.add(DevicesDao.ATTR_ID_DEV);
                query = this.devicesService.deviceQuery(data,attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(DevicesDao.ATTR_ID_DEV));
                } else {
                    toret = this.devicesService.deviceInsert(data);
                    entry.setValue(toret.get(DevicesDao.ATTR_ID_DEV));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_PLATE)) {
                data.put(PlatesDao.ATTR_PLATE_NUMBER, entry.getValue());
                attr.add(PlatesDao.ATTR_ID_PLATE);
                query = this.platesService.plateQuery(data, attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(PlatesDao.ATTR_ID_PLATE));
                }else{
                    toret = this.platesService.plateInsert(data);
                    entry.setValue(toret.get(PlatesDao.ATTR_ID_PLATE));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_TRAILER_PLATE)) {
                data.put(TrailerPlatesDao.ATTR_TRAILER_PLATE_NUMBER, entry.getValue());
                attr.add(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE);
                query = this.trailerPlatesService.trailerPlateQuery(data, attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE));
                } else {
                    toret = this.trailerPlatesService.trailerPlateInsert(data);
                    entry.setValue(toret.get(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_DELIVERY_NOTE)) {
                data.put(DeliveryNotesDao.ATTR_DELIVERY_NAME, entry.getValue());
                attr.add(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE);
                query = this.deliverynotesService.deliverynotesQuery(data, attr);
                if(query.calculateRecordNumber() > 0){
                    entry.setValue(query.getRecordValues(0).get(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE));
                }else {
                    toret = this.deliverynotesService.deliverynotesInsert(data);
                    entry.setValue(toret.get(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE));
                }
            }
        }

        return attrMap;
    }

    private Map<String,Object> updateNonRelatedData(Map<String, Object> inputMap){
        Map<String, Object> attrMap = new HashMap<>(inputMap);

        for(Map.Entry<String, Object>entry : attrMap.entrySet()){
            Map<String, Object> data = new HashMap<>();
            List<String> attr = new ArrayList<>();
            EntityResult toret;
            EntityResult query;

            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_DEV_OUT)) {
                data.put(DevicesDao.ATTR_DEVICE_NAME, entry.getValue());
                attr.add(DevicesDao.ATTR_ID_DEV);
                query = this.devicesService.deviceQuery(data,attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(DevicesDao.ATTR_ID_DEV));
                } else {
                    toret = this.devicesService.deviceInsert(data);
                    entry.setValue(toret.get(DevicesDao.ATTR_ID_DEV));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_PLATE)) {
                data.put(PlatesDao.ATTR_PLATE_NUMBER, entry.getValue());
                attr.add(PlatesDao.ATTR_ID_PLATE);
                query = this.platesService.plateQuery(data, attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(PlatesDao.ATTR_ID_PLATE));
                }else{
                    toret = this.platesService.plateInsert(data);
                    entry.setValue(toret.get(PlatesDao.ATTR_ID_PLATE));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_TRAILER_PLATE)) {
                data.put(TrailerPlatesDao.ATTR_TRAILER_PLATE_NUMBER, entry.getValue());
                attr.add(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE);
                query = this.trailerPlatesService.trailerPlateQuery(data, attr);
                if(query.calculateRecordNumber() > 0 ) {
                    entry.setValue(query.getRecordValues(0).get(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE));
                } else {
                    toret = this.trailerPlatesService.trailerPlateInsert(data);
                    entry.setValue(toret.get(TrailerPlatesDao.ATTR_ID_TRAILER_PLATE));
                }
            }
            if(entry.getKey().equalsIgnoreCase(TravelsDao.ATTR_ID_DELIVERY_NOTE)) {
                data.put(DeliveryNotesDao.ATTR_DELIVERY_NAME, entry.getValue());
                attr.add(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE);
                query = this.deliverynotesService.deliverynotesQuery(data, attr);
                if(query.calculateRecordNumber() > 0){
                    entry.setValue(query.getRecordValues(0).get(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE));
                }else {
                    toret = this.deliverynotesService.deliverynotesInsert(data);
                    entry.setValue(toret.get(DeliveryNotesDao.ATTR_ID_DELIVERY_NOTE));
                }
            }
        }

        return attrMap;
    }
}