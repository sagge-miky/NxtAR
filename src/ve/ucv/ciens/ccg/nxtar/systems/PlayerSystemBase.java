/*
 * Copyright (C) 2013 Miguel Angel Astor Romero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.components.PlayerComponentBase;
import ve.ucv.ciens.ccg.nxtar.scenarios.ScenarioGlobals;
import ve.ucv.ciens.ccg.nxtar.scenarios.SummaryBase;

import com.artemis.Aspect;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.utils.Disposable;

public abstract class PlayerSystemBase extends EntityProcessingSystem implements Disposable{
	protected NxtARCore core;

	@SuppressWarnings("unchecked")
	public PlayerSystemBase(Class<? extends PlayerComponentBase> component){
		super(Aspect.getAspectForAll(component));
	}

	public abstract SummaryBase getPlayerSummary();

	public final void setCore(NxtARCore core) throws IllegalArgumentException{
		if(core == null)
			throw new IllegalArgumentException("Core is null.");

		this.core = core;
	}

	protected final void finishGame(boolean victory) throws IllegalStateException{
		if(core == null)
			throw new IllegalStateException("Core is null.");

		ScenarioGlobals.getEntityCreator().resetAllEntities();
		core.nextState = NxtARCore.game_states_t.SCENARIO_END_SUMMARY;
	}
}
