/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.storage.IntsRef;

import java.util.regex.Pattern;

/**
 * https://wiki.openstreetmap.org/wiki/Key:level
 */
public class OSMLevelParser implements TagParser {
    // Splits levels by semicolon (e.g. "0;1") or range hyphen (e.g. "0-2" or "-2 - -1").
    // Lookbehind (?<=\d) ensures the hyphen is preceded by a digit, preventing leading minus signs (e.g. "-1") from being treated as range separators.
    // Lookahead (?=-?\d) ensures the hyphen is followed by a number.
    private static final Pattern LEVEL_SPLIT_PATTERN = Pattern.compile(";|(?<=\\d)\\s*-\\s*(?=-?\\d)");

    private final DecimalEncodedValue levelEnc;

    public OSMLevelParser(DecimalEncodedValue levelEnc) {
        this.levelEnc = levelEnc;
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        double level = 0;

        if (way.hasTag("level")) {
            String levels = way.getTag("level");

            String[] levelsTok = LEVEL_SPLIT_PATTERN.split(levels);

            if (levelsTok.length == 1) {
                try {
                    level = Double.parseDouble(levelsTok[0]);
                } catch (NumberFormatException ex) {
                    // ignore if no number
                }
            } else if (levelsTok.length == 2) {
                try {
                    double first = Double.parseDouble(levelsTok[0]);
                    double second = Double.parseDouble(levelsTok[1]);
                    level = (first + second)/2;
                } catch (NumberFormatException ex) {
                    // ignore if no number
                }
            }
        }
        level = Math.max(levelEnc.getMinStorableDecimal(), Math.min(levelEnc.getMaxStorableDecimal(), level));
        levelEnc.setDecimal(false, edgeId, edgeIntAccess, level);
    }
}
