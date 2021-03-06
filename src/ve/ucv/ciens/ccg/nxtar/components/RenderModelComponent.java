/*
 * Copyright (C) 2014 Miguel Angel Astor Romero
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
package ve.ucv.ciens.ccg.nxtar.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class RenderModelComponent extends Component {
	public ModelInstance instance;

	public RenderModelComponent(Model model) throws IllegalArgumentException{
		if(model == null)
			throw new IllegalArgumentException("Model is null.");

		this.instance = new ModelInstance(model);
	}

	public RenderModelComponent(ModelInstance instance) throws IllegalArgumentException{
		if(instance == null)
			throw new IllegalArgumentException("Instance is null.");

		this.instance = instance;
	}
}
