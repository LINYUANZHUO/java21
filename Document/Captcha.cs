using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Text;
using System.Web;
 
namespace Demo
{
    /// <summary>
    /// Captcha
    /// </summary>
    public class Captcha
    {
        #region Private Field
        /// <summary>
        /// �������
        /// </summary>
        private Random objRandom = new Random();
        #endregion
 
        #region Public Property
 
        #region ��֤�볤��
        /// <summary>
        /// ��֤�볤��
        /// </summary>
        private int length = 4;
        /// <summary>
        /// ��֤�볤��(Ĭ��Ϊ4)
        /// </summary>
        public int Length
        {
            get { return this.length; }
            set { this.length = value; }
        }
        #endregion
 
        #region ��֤���ַ���
        /// <summary>
        /// ��֤���ַ���
        /// </summary>
        private string verifyCodeText = null;
 
        /// <summary>
        /// ��֤���ַ���
        /// </summary>
        public string VerifyCodeText
        {
            get { return this.verifyCodeText; }
            set { this.verifyCodeText = value; }
        }
        #endregion
 
        #region �Ƿ����Сд��ĸ
        /// <summary>
        /// �Ƿ����Сд��ĸ
        /// </summary>
        private bool addLowerLetter = false;
 
        /// <summary>
        /// �Ƿ����Сд��ĸ(������o)
        /// </summary>
        public bool AddLowerLetter
        {
            get { return this.addLowerLetter; }
            set { this.addLowerLetter = value; }
        }
        #endregion
 
        #region �Ƿ�����д��ĸ
        /// <summary>
        /// �Ƿ�����д��ĸ
        /// </summary>
        private bool addUpperLetter = false;
 
        /// <summary>
        /// �Ƿ�����д��ĸ(������O)
        /// </summary>
        public bool AddUpperLetter
        {
            get { return this.addUpperLetter; }
            set { this.addUpperLetter = value; }
        }
        #endregion
 
        #region �����С
        /// <summary>
        /// �����С
        /// </summary>
        private int fontSize = 18;
 
        /// <summary>
        /// �����С(Ĭ��Ϊ18)
        /// </summary>
        public int FontSize
        {
            get { return this.fontSize; }
            set { this.fontSize = value; }
        }
        #endregion
 
        #region ������ɫ
        /// <summary>
        /// ������ɫ
        /// </summary>
        private Color fontColor = Color.Blue;
 
        /// <summary>
        /// ������ɫ(Ĭ��ΪBlue)
        /// </summary>
        public Color FontColor
        {
            get { return this.fontColor; }
            set { this.fontColor = value; }
        }
        #endregion
 
        #region ��������
        /// <summary>
        /// ��������
        /// </summary>
        private string fontFamily = "Verdana";
 
        /// <summary>
        /// ��������(Ĭ��ΪVerdana)
        /// </summary>
        public string FontFamily
        {
            get { return this.fontFamily; }
            set { this.fontFamily = value; }
        }
        #endregion
 
        #region ����ɫ
        /// <summary>
        /// ����ɫ
        /// </summary>
        private Color backgroundColor = Color.AliceBlue;
 
        /// <summary>
        /// ����ɫ(Ĭ��ΪAliceBlue)
        /// </summary>
        public Color BackgroundColor
        {
            get { return this.backgroundColor; }
            set { this.backgroundColor = value; }
        }
        #endregion
 
        #region ǰ���������
        /// <summary>
        /// ǰ���������
        /// </summary>
        private int foreNoisePointCount = 2;
 
        /// <summary>
        /// ǰ���������(Ĭ��Ϊ2)
        /// </summary>
        public int ForeNoisePointCount
        {
            get { return this.foreNoisePointCount; }
            set { this.foreNoisePointCount = value; }
        }
        #endregion
 
        #region ��������ת�Ƕ�
        /// <summary>
        /// ��������ת�Ƕ�
        /// </summary>
        private int randomAngle = 45;
 
        /// <summary>
        /// ��������ת�Ƕ�(Ĭ��Ϊ40��)
        /// </summary>
        public int RandomAngle
        {
            get { return this.randomAngle; }
            set { this.randomAngle = value; }
        }
        #endregion
 
        #endregion
 
        #region Constructor Method
        /// <summary>
        /// ���췽��
        /// </summary>
        public Captcha()
        {
            this.GetText();
        }
        #endregion
 
        #region Private Method
        /// <summary>
        /// �õ���֤���ַ���
        /// </summary>
        private void GetText()
        {
            //û���ⲿ������֤��ʱ�������
            if (String.IsNullOrEmpty(this.verifyCodeText))
            {
                StringBuilder objStringBuilder = new StringBuilder();
 
                //��������1-9
                for (int i = 1; i <= 9; i++)
                {
                    objStringBuilder.Append(i.ToString());
                }
 
                //�����д��ĸA-Z��������O
                if (this.addUpperLetter)
                {
                    char temp = ' ';
 
                    for (int i = 0; i < 26; i++)
                    {
                        temp = Convert.ToChar(i + 65);
 
                        //������ɵ���ĸ����'O'
                        if (!temp.Equals('O'))
                        {
                            objStringBuilder.Append(temp);
                        }
                    }
                }
 
                //����Сд��ĸa-z��������o
                if (this.addLowerLetter)
                {
                    char temp = ' ';
 
                    for (int i = 0; i < 26; i++)
                    {
                        temp = Convert.ToChar(i + 97);
 
                        //������ɵ���ĸ����'o'
                        if (!temp.Equals('o'))
                        {
                            objStringBuilder.Append(temp);
                        }
                    }
                }
 
                //������֤���ַ���
                {
                    int index = 0;
 
                    for (int i = 0; i < length; i++)
                    {
                        index = objRandom.Next(0, objStringBuilder.Length);
 
                        this.verifyCodeText += objStringBuilder[index];
 
                        objStringBuilder.Remove(index, 1);
                    }
                }
            }
        }
        /// <summary>
        /// �õ���֤��ͼƬ
        /// </summary>
        private Bitmap GetImage()
        {
            Bitmap result = null;
 
            //������ͼ
            result = new Bitmap(this.verifyCodeText.Length * 16, 25);
 
            using (Graphics objGraphics = Graphics.FromImage(result))
            {
                objGraphics.SmoothingMode = SmoothingMode.HighQuality;
 
                //���������ͼ�沢��ָ������ɫ���
                objGraphics.Clear(this.backgroundColor);
 
                //��������
                using (SolidBrush objSolidBrush = new SolidBrush(this.fontColor))
                {
                    this.AddForeNoisePoint(result);
 
                    this.AddBackgroundNoisePoint(result, objGraphics);
 
                    //���־���
                    StringFormat objStringFormat = new StringFormat(StringFormatFlags.NoClip);
 
                    objStringFormat.Alignment = StringAlignment.Center;
                    objStringFormat.LineAlignment = StringAlignment.Center;
 
                    //������ʽ
                    Font objFont = new Font(this.fontFamily, objRandom.Next(this.fontSize - 3, this.fontSize), FontStyle.Regular);
 
                    //��֤����ת����ֹ����ʶ��
                    char[] chars = this.verifyCodeText.ToCharArray();
 
                    for (int i = 0; i < chars.Length; i++)
                    {
                        //ת���Ķ���
                        float angle = objRandom.Next(-this.randomAngle, this.randomAngle);
 
                        objGraphics.TranslateTransform(12, 12);
                        objGraphics.RotateTransform(angle);
                        objGraphics.DrawString(chars[i].ToString(), objFont, objSolidBrush, -2, 2, objStringFormat);
                        objGraphics.RotateTransform(-angle);
                        objGraphics.TranslateTransform(2, -12);
                    }
                }
            }
 
            return result;
        }
        /// <summary>
        /// ���ǰ�����
        /// </summary>
        /// <param name="objBitmap"></param>
        private void AddForeNoisePoint(Bitmap objBitmap)
        {
            for (int i = 0; i < objBitmap.Width * this.foreNoisePointCount; i++)
            {
                objBitmap.SetPixel(objRandom.Next(objBitmap.Width), objRandom.Next(objBitmap.Height), this.fontColor);
            }
        }
        /// <summary>
        /// ��ӱ������
        /// </summary>
        /// <param name="objBitmap"></param>
        /// <param name="objGraphics"></param>
        private void AddBackgroundNoisePoint(Bitmap objBitmap, Graphics objGraphics)
        {
            using (Pen objPen = new Pen(Color.Azure, 0))
            {
                for (int i = 0; i < objBitmap.Width * 2; i++)
                {
                    objGraphics.DrawRectangle(objPen, objRandom.Next(objBitmap.Width), objRandom.Next(objBitmap.Height), 1, 1);
                }
            }
        }
        #endregion
 
        #region Public Method
        public void Output(HttpResponse objHttpResponse)
        {
            using (Bitmap objBitmap = this.GetImage())
            {
                if (objBitmap != null)
                {
                    using (MemoryStream objMS = new MemoryStream())
                    {
                        objBitmap.Save(objMS, ImageFormat.Jpeg);
 
                        HttpContext.Current.Response.ClearContent();
                        HttpContext.Current.Response.ContentType = "image/Jpeg";
                        HttpContext.Current.Response.BinaryWrite(objMS.ToArray());
                        HttpContext.Current.Response.Flush();
                        HttpContext.Current.Response.End();
                    }
                }
            }
        }
        #endregion
    }
}