package com.swak.formula.executor;

import com.swak.common.dto.Response;
import com.swak.formula.entity.SimulationResult;
import com.swak.formula.entity.VirtualSimulationCmd;

public interface FormulaSimulation {
    /**
     * 模拟
     * @return
     */
    Response<SimulationResult> simulation(VirtualSimulationCmd command);

      static FormulaSimulation getInstance() {
        return SimulationInstance.INSTANCE;
    }
     class SimulationInstance {
        private static final FormulaSimulation INSTANCE = new VirtualSimulation();
    }
}
