package org.psem2m.utilities.system;
//
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public interface IXOSCommand
{
  /**
	 * CallBack lors de l'ex�cution d'un commande system
	 * Voir classes CXOSUtils, CXOSCommand et java.lang.Process, java.lang.RunTime
	 * 
	 * @param aBufferInput
	 * -> Input standard pour envoyer des donn�es � la commande systeme
	 * -> !!!! Tester si null ou pas avant d'�crire
	 * ---> On force null pour un dernier appel callBack, lorsque le process c'est termin�
	 * @param aBufferOutput 
	 * -> Output standard renvoy� par la commande systeme
	 * @param aBufferOutputErr 
	 * -> Output standard renvoy� par la commande systeme en cas d'erreur
	 * @param aElaspedTime
	 * -> Temps �coul�
	 * @return 
	 * --> true pour continuer le process
	 * --> false pour arr�ter le process
	 * @throws Exception
	 */
	public boolean runCallBack(	OutputStream 	aBufferInput,
																InputStream 	aBufferOutput,
																InputStream 	aBufferOutputErr,
																long 				aElaspedTime) 
															throws Exception;
	
}
