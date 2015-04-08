/*
 * The MIT License
 *
 * Copyright 2014 Miroslav Cupak (mirocupak@gmail.com).
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
package com.dnastack.bob.service.impl;

import com.dnastack.bob.persistence.api.BeaconDao;
import com.dnastack.bob.persistence.entity.Beacon;
import com.dnastack.bob.service.api.BeaconService;
import com.dnastack.bob.service.dto.BeaconTo;
import com.dnastack.bob.service.util.Entity2ToConvertor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Implementation of a service managing beacons.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Stateless
@Local(BeaconService.class)
@Named
@Transactional
public class BeaconServiceImpl implements BeaconService {

    @Inject
    private BeaconDao beaconDao;

    @Override
    public BeaconTo getBeacon(String beaconId) {
        Beacon b = beaconDao.findById(beaconId);
        return Entity2ToConvertor.getBeaconTo((b == null || !b.getVisible()) ? null : b);
    }

    @Override
    public Collection<BeaconTo> getBeacons(Collection<String> beaconIds) {
        List<BeaconTo> res = new ArrayList<>();
        for (String id : beaconIds) {
            BeaconTo b = getBeacon(id);
            if (b != null) {
                res.add(b);
            }
        }

        return res;
    }

    @Override
    public Collection<BeaconTo> getAll() {
        return Entity2ToConvertor.getBeaconTos(beaconDao.findByVisibility(true));
    }

}
