package com.ontimize.hr.api.core.service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

import java.util.List;
import java.util.Map;

public interface ICompaniesService {
    public EntityResult companyQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
    public EntityResult companyInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
    public EntityResult companyUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
    public EntityResult companyDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
}
