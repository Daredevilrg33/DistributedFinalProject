package dlms;


/**
* dlms/_FrontEndOperationsStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FrontEndOperations.idl
* Wednesday, March 20, 2019 12:51:12 AM EDT
*/

public class _FrontEndOperationsStub extends org.omg.CORBA.portable.ObjectImpl implements dlms.FrontEndOperations
{

  public String borrowItem (String userId, String itemId, int noOfDays)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("borrowItem", true);
                $out.write_string (userId);
                $out.write_string (itemId);
                $out.write_long (noOfDays);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return borrowItem (userId, itemId, noOfDays        );
            } finally {
                _releaseReply ($in);
            }
  } // borrowItem

  public String findItem (String userId, String itemName)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("findItem", true);
                $out.write_string (userId);
                $out.write_string (itemName);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return findItem (userId, itemName        );
            } finally {
                _releaseReply ($in);
            }
  } // findItem

  public boolean returnItem (String userId, String itemId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("returnItem", true);
                $out.write_string (userId);
                $out.write_string (itemId);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return returnItem (userId, itemId        );
            } finally {
                _releaseReply ($in);
            }
  } // returnItem

  public boolean exchangeItem (String userId, String newItemId, String oldItemId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("exchangeItem", true);
                $out.write_string (userId);
                $out.write_string (newItemId);
                $out.write_string (oldItemId);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return exchangeItem (userId, newItemId, oldItemId        );
            } finally {
                _releaseReply ($in);
            }
  } // exchangeItem

  public boolean addItem (String managerId, String itemId, String itemName, int quantity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("addItem", true);
                $out.write_string (managerId);
                $out.write_string (itemId);
                $out.write_string (itemName);
                $out.write_long (quantity);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return addItem (managerId, itemId, itemName, quantity        );
            } finally {
                _releaseReply ($in);
            }
  } // addItem

  public boolean removeItem (String managerId, String itemId, int quantity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("removeItem", true);
                $out.write_string (managerId);
                $out.write_string (itemId);
                $out.write_long (quantity);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return removeItem (managerId, itemId, quantity        );
            } finally {
                _releaseReply ($in);
            }
  } // removeItem

  public String listItemAvailability (String managerId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("listItemAvailability", true);
                $out.write_string (managerId);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return listItemAvailability (managerId        );
            } finally {
                _releaseReply ($in);
            }
  } // listItemAvailability

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:dlms/FrontEndOperations:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _FrontEndOperationsStub