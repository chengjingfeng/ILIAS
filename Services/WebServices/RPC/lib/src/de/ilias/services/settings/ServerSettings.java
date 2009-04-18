/*
        +-----------------------------------------------------------------------------+
        | ILIAS open source                                                           |
        +-----------------------------------------------------------------------------+
        | Copyright (c) 1998-2001 ILIAS open source, University of Cologne            |
        |                                                                             |
        | This program is free software; you can redistribute it and/or               |
        | modify it under the terms of the GNU General Public License                 |
        | as published by the Free Software Foundation; either version 2              |
        | of the License, or (at your option) any later version.                      |
        |                                                                             |
        | This program is distributed in the hope that it will be useful,             |
        | but WITHOUT ANY WARRANTY; without even the implied warranty of              |
        | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               |
        | GNU General Public License for more details.                                |
        |                                                                             |
        | You should have received a copy of the GNU General Public License           |
        | along with this program; if not, write to the Free Software                 |
        | Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. |
        +-----------------------------------------------------------------------------+
 */

package de.ilias.services.settings;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 * Stores general server settings like rpc host and port, global log file and
 * log level.
 * 
 * @author Stefan Meyer <smeyer.ilias@gmx.de>
 * @version $Id$
 */
public class ServerSettings {

	private static Logger logger = Logger.getLogger(ServerSettings.class);
	private static ServerSettings instance = null;


	private InetAddress host;
	private int port;

	private File indexPath;
	private File logFile;
	private Level logLevel;
	private int numThreads = 1;



	/**
	 * @param properties
	 */
	private ServerSettings() {

	}
	
	/**
	 * Global singleton for all threads
	 * @return
	 * @throws ConfigurationException
	 */
	public static synchronized ServerSettings getInstance() throws ConfigurationException {

		if (instance == null) {
			instance = new ServerSettings();
		}
		return instance;
	}

	/**
	 * @return the host
	 */
	public InetAddress getHost() {
		return host;
	}

	/**
	 * @param host
	 *            The host to set.
	 * @throws ConfigurationException
	 */
	public void setHost(String host) throws ConfigurationException {

		try {
			this.host = InetAddress.getByName(host);
		} 
		catch (UnknownHostException e) {
			logger.fatal("Unknown host given: " + host);
			throw new ConfigurationException(e);
		}
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	/**
	 * @return the indexPath
	 */
	public File getIndexPath() {
		return indexPath;
	}
	
	/**
	 * @return the logFile
	 */
	public File getLogFile() {
		return logFile;
	}

	/**
	 * @param logFile the logFile to set
	 * @throws ConfigurationException 
	 * @throws IOException , ConfigurationException
	 */
	public void setLogFile(String logFile) throws ConfigurationException, IOException {

		this.logFile = new File(logFile);
		if(!this.logFile.isAbsolute()) {
			throw new ConfigurationException("Absolute path to log file required: " + logFile);
		}
		if(this.logFile.isDirectory()) {
			throw new ConfigurationException("Absolute path to logfile required: " + logFile);
		}
		if(this.logFile.createNewFile()) {
			System.out.println("Created new log file: " + this.logFile.getAbsolutePath());
		}
		else {
			System.out.println("Using existing log file: " + this.logFile.getAbsolutePath());
		}
		if(!this.logFile.canWrite()) {
			throw new ConfigurationException("Cannot write to log file: " + logFile);
		}
	}
	

	/**
	 * @return the logLevel
	 */
	public Level getLogLevel() {
		return logLevel;
	}

	/**
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(String logLevel) {

		this.logLevel = Level.toLevel(logLevel.trim(),Level.INFO);
	}

	/**
	 * @param indexPath
	 *            the indexPath to set
	 * @throws ConfigurationException
	 */
	public void setIndexPath(String indexPath) throws ConfigurationException {

		this.indexPath = new File(indexPath);

		if (!this.indexPath.isAbsolute()) {
			throw new ConfigurationException("Absolute path required: " + indexPath);
		}
		if (!this.indexPath.canWrite()) {
			throw new ConfigurationException("Path not writable: " + indexPath);
		}
		if (!this.indexPath.isDirectory()) {
			throw new ConfigurationException("Directory name required: " + indexPath);
		}
	}
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public void initLogger() throws ConfigurationException {

		Logger logger = Logger.getRootLogger();
        logger.setLevel(Level.INFO);

        try {
            RollingFileAppender file_appender = new RollingFileAppender(
                    new PatternLayout("%d{ISO8601} %-5p %t (%F:%L) - %m%n"),
                    this.logFile.getAbsolutePath());
            
            // TODO: increase max file size
            file_appender.setMaxFileSize("100MB");
            logger.removeAllAppenders();
            logger.addAppender(file_appender);
            logger.setLevel(this.getLogLevel());
            
            logger.info("Started loggin to: " + getLogFile().getAbsolutePath());
            logger.info("Using log level " + getLogLevel());
        } 
        catch (IOException e) {
        	logger.fatal("Error appending log file.");
            throw new ConfigurationException(e);
        }

	}

	/**
	 * @param purgeString
	 */
	public void setThreadNumber(String purgeString) {

		this.numThreads = Integer.valueOf(purgeString);
	}
	
	public int getNumThreads() {
		return numThreads;
	}
}
