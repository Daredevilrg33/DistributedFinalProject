package dlms;


/**
* dlms/FrontEndOperationsHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FrontEndOperations.idl
* Wednesday, March 20, 2019 12:51:12 AM EDT
*/

abstract public class FrontEndOperationsHelper
{
  private static String  _id = "IDL:dlms/FrontEndOperations:1.0";

  public static void insert (org.omg.CORBA.Any a, dlms.FrontEndOperations that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static dlms.FrontEndOperations extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (dlms.FrontEndOperationsHelper.id (), "FrontEndOperations");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static dlms.FrontEndOperations read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FrontEndOperationsStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, dlms.FrontEndOperations value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static dlms.FrontEndOperations narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof dlms.FrontEndOperations)
      return (dlms.FrontEndOperations)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      dlms._FrontEndOperationsStub stub = new dlms._FrontEndOperationsStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static dlms.FrontEndOperations unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof dlms.FrontEndOperations)
      return (dlms.FrontEndOperations)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      dlms._FrontEndOperationsStub stub = new dlms._FrontEndOperationsStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
