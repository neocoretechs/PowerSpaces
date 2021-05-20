package com.neocoretechs.powerspaces.server.handler;

public class AccessAttribute
{
    public static final String _READ = "Read";
    public static final String _WRITE = "Write";
    public static final String _ADMIN = "Admin";
    public static final String _OWNER = "Owner";
    public static final String _CREATE = "Create";
    private String Aattribute;
    public static final AccessAttribute _READ_ATTRIBUTE = new AccessAttribute("Read");
    public static final AccessAttribute _WRITE_ATTRIBUTE = new AccessAttribute("Write");
    public static final AccessAttribute _ADMIN_ATTRIBUTE = new AccessAttribute("Admin");
    public static final AccessAttribute _OWNER_ATTRIBUTE = new AccessAttribute("Owner");
    public static final AccessAttribute _CREATE_ATTRIBUTE = new AccessAttribute("Create");

    public AccessAttribute(String tattribute)
    {
        Aattribute = tattribute;
    }

    public synchronized String attribute()
    {
        return Aattribute;
    }
}
