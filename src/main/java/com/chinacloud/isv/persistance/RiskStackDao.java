package com.chinacloud.isv.persistance;

import java.util.ArrayList;

import com.chinacloud.isv.domain.RiskStack;

public interface RiskStackDao {
	public ArrayList<RiskStack> getRisks();
	public void addRisk(RiskStack risk);
	public void deleteRisk(String id);
}
