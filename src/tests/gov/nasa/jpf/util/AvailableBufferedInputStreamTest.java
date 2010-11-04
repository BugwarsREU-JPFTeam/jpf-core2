package gove.nasa.jpf.util;

import gov.nasa.jpf.util.*;
import java.io.*;
import java.util.concurrent.*;
import org.junit.*;

public class AvailableBufferedInputStreamTest
{
   private static final int PIPE_SIZE = 1024 * 1024;

   private PipedOutputStream            m_output;
   private AvailableBufferedInputStream m_input;
   
   @Before
   public void before() throws IOException
   {
      m_output = new PipedOutputStream();
      m_input  = new AvailableBufferedInputStream(new PipedInputStream(m_output, PIPE_SIZE));
   }
   
   @After
   public void after() throws IOException
   {
      m_output.flush();
      m_output.close();
      m_output = null;

      Assert.assertEquals( 0, m_input.available());
      Assert.assertEquals(-1, m_input.peek());
      Assert.assertEquals(-1, m_input.read());

      m_input.close();
      m_input = null;
   }
   
   @Test(expected = NullPointerException.class)
   public void passNullPointerToConstructor() throws IOException
   {
      new AvailableBufferedInputStream(null).close();
   }
   
   @Test
   public void availableStuck() throws IOException
   {
      int i;
      
      for (i = 10; --i >= 0; )
      {
         m_output.write(i);
         m_output.flush();
         Assert.assertEquals(1, m_input.available());
      }

      for (i = 10; --i >= 0; )
      {
         Assert.assertEquals(i, m_input.peek());
         Assert.assertEquals(i, m_input.read());
      }
   }
   
   @Test
   public void unreadExtra() throws IOException
   {
      int i;
      
      m_output.write(20);
      m_output.write(21);
      m_output.flush();

      Assert.assertEquals(20, m_input.peek());
      Assert.assertEquals(20, m_input.read());  // Load the buffer
      
      for (i = 0; i < 10; i++)
         m_input.unread(i);
      
      for (i = 10; --i >= 0; )
      {
         Assert.assertEquals(i, m_input.peek());
         Assert.assertEquals(i, m_input.read());
         Assert.assertEquals(i + 1, m_input.available());
      }

      Assert.assertEquals(21, m_input.peek());
      Assert.assertEquals(21, m_input.read());
   }
   
   @Test
   public void unreadMinus1() throws IOException
   {
      m_input.unread(-1);
      Assert.assertEquals(0x00FF, m_input.peek());
      Assert.assertEquals(0x00FF, m_input.read());
   }
   
   @Test
   public void readBufferSplit() throws IOException
   {
      byte buffer[];
      
      buffer = new byte[2];
      
      m_output.write(30);
      m_output.flush();
      
      m_input.available();
      
      m_output.write(40);
      m_output.flush();

      Assert.assertEquals(30, m_input.peek());
      Assert.assertEquals(1,  m_input.read(buffer));
      Assert.assertEquals(30, buffer[0]);

      Assert.assertEquals(40, m_input.peek());
      Assert.assertEquals(1,  m_input.read(buffer));
      Assert.assertEquals(40, buffer[0]);
   }

   @Test
   public void readBufferPartialNoBlock() throws IOException
   {
      byte buffer[];

      buffer = new byte[2];

      m_output.write(30);
      m_output.flush();

      Assert.assertEquals(30, m_input.peek());
      Assert.assertEquals(1,  m_input.read(buffer));
      Assert.assertEquals(30, buffer[0]);
   }

   @Test
   public void readBufferLeftOver() throws IOException
   {
      byte buffer[];

      buffer = new byte[2];

      m_output.write(30);
      m_output.write(40);
      m_output.write(50);
      m_output.flush();

      Assert.assertEquals(30, m_input.peek());
      Assert.assertEquals(2, m_input.read(buffer));
      Assert.assertEquals(30, buffer[0]);
      Assert.assertEquals(40, buffer[1]);

      Assert.assertEquals(50, m_input.peek());
      Assert.assertEquals(1, m_input.read(buffer));
      Assert.assertEquals(50, buffer[0]);
      Assert.assertEquals(40, buffer[1]);
   }
   
   @Test
   public void unreadOverflow() throws IOException
   {
      int i;
      
      try
      {
         for (i = 0; i < m_input.getBufferSize(); i++)
         {
            m_input.unread(i);
            Assert.assertEquals(i & 0x00FF, m_input.peek());
         }
      }
      catch (IOException e)
      {
         Assert.fail();
      }
      
      try
      {
         m_input.unread(0);
         Assert.fail();
      }
      catch (IOException e)
      {
         e = null;  // Get rid of IDE warning
      }

      for (i = m_input.getBufferSize(); --i >= 0; )
      {
         Assert.assertEquals(i & 0x00FF, m_input.peek());
         Assert.assertEquals(i & 0x00FF, m_input.read());
      }
   }
   
   @Test
   public void fillWithNoMoreData() throws IOException
   {
      Assert.assertEquals(0, m_input.available());
      Assert.assertEquals(0, m_input.available());
   }
   
   @Test
   public void fillWithTooMuchData() throws IOException
   {
      int i;
      
      for (i = 0; i < m_input.getBufferSize() + 1; i++)
         m_output.write(i);
      
      m_output.flush();
      
      Assert.assertEquals(m_input.getBufferSize(), m_input.available());

      for (i = 0; i < m_input.getBufferSize() + 1; i++)
      {
         Assert.assertEquals(i & 0x00FF, m_input.peek());
         Assert.assertEquals(i & 0x00FF, m_input.read());
      }
   }
   
   @Test
   public void readAfterClose() throws IOException
   {
      m_output.write(10);
      m_output.flush();
      
      m_input.available();
      
      m_output.close();

      Assert.assertEquals(10, m_input.peek());
      Assert.assertEquals(10, m_input.read());
      Assert.assertEquals(-1, m_input.peek());
      Assert.assertEquals(-1, m_input.read());
   }

   @Test
   public void readBufferAfterClose() throws IOException
   {
      byte buffer[];
      
      m_output.write(10);
      m_output.flush();

      m_input.available();

      m_output.close();

      buffer = new byte[10];

      Assert.assertEquals(1, m_input.read(buffer));
      Assert.assertEquals(-1, m_input.read(buffer));
   }
   
   @Test
   public void testToString() throws IOException
   {
      int i;
      
      Assert.assertEquals("", m_input.toString());

      m_output.write(new byte[]{'h', 'e', 'l', 'l', 'o'});
      m_output.flush();
      
      m_input.available();
      
      Assert.assertEquals("hello", m_input.toString());
      
      for (i = 5; --i >= 0; )
         m_input.read();
   }
   
   @Test
   public void readBufferEmptyBuffer() throws IOException
   {
      byte buffer[];
      
      m_output.write(10);
      m_output.flush();
      
      buffer = new byte[1];
      
      Assert.assertEquals(1,  m_input.read(buffer));
      Assert.assertEquals(10, buffer[0]);
   }
}