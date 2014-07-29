package org.rhq.audit.common;

import junit.framework.Assert;

import org.testng.annotations.Test;

@Test
public class SubsystemTest {
    public void testEquality() {
        Assert.assertEquals(Subsystem.MISCELLANEOUS, Subsystem.MISCELLANEOUS);
        Assert.assertEquals(new Subsystem("foo"), new Subsystem("foo"));
        Assert.assertFalse(new Subsystem("foo").equals(new Subsystem("bar")));
    }
}
