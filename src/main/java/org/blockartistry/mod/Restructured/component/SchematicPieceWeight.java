/*
 * This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.Restructured.component;

import java.util.HashMap;

import org.blockartistry.mod.Restructured.assets.Assets;
import org.blockartistry.mod.Restructured.assets.SchematicProperties;

import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;

/**
 * To make this thing go a specially crafted PieceWeight will
 * act as a factory.  When processed by the SchematicStructure
 * handler it will potentially pick a new structure each
 * time the handler is invoked.
 * 
 * We use the total weight from the internal weight table when
 * registering with the village generation logic.  Statistically
 * this should balance out the fact that each individual schematic
 * does not have a class and has to work through the
 * SchematicStructure proxy.
 *
 */
public class SchematicPieceWeight extends PieceWeight {

	// Maintain a history of the schematics that came
	// through this factory.  Still have to apply weights
	// and limits based on configuration.
	protected HashMap<SchematicProperties, Integer> history = new HashMap<SchematicProperties, Integer>();
	
	// Cached set of properties that have been generated
	protected SchematicProperties properties;
	
	public SchematicPieceWeight() {
		super(SchematicStructure.class, Assets.villageStructureTotalWeight(), 10);
		
		this.properties = null;
	}
	
	public SchematicProperties getProperties() {
		return this.properties;
	}
	
	/**
	 * Generates a new schematic to use when passing back the
	 * SchematicStructure instance.
	 * 
	 * @return Properties to use.  Will return null if there is nothing else.
	 */
	public SchematicProperties getNextStructure() {
		SchematicProperties props = null;
		
		do {
			props = Assets.getNextVillageStructure();
			if(history.containsKey(props)) {
				int runCount = history.get(props);
				if(runCount >= props.limit)
					props = null;
				else
					history.put(props, ++runCount);
			}
			else
				history.put(props, 1);
		}
		while(props == null && history.size() < Assets.villageStructureCount());
		
		this.properties = props;
		return props;
	}
}