import axios from "axios"

const EMAIL_SERVICE_URL = process.env.EMAIL_SERVICE_URL || "http://email-service:4007";

export const sendVerificationEmail = async (email: string, token: string) => {
  try {
    const response = await axios.post(`${EMAIL_SERVICE_URL}/sendMail/verification`, {
      email,
      token 
    })
    return response.data;
  } catch (error) {
    console.log('[sendVerificationEmail] error:', (error as any)?.message || error);
    throw error;
  }
}